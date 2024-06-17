<template>
  <!-- Navigation -->
  <nav class="navbar bg-body-tertiary">
    <div class="container-fluid">
      <a class="navbar-brand">클래스룸</a>
      <form class="d-flex" role="search">
        <div class="card">
          <div class="card-body d-flex">
            <h5>http://localhost:5173/student/{{ classCode }}</h5>
            <button @click="changeModalData('반 코드')" class="btn btn-sm btn-outline-secondary" type="button" 
            data-bs-toggle="modal" data-bs-target="#exampleModalCenter">코드</button>
            <button @click="changeModalData('QR 코드')" class="btn btn-sm btn-outline-secondary" type="button" 
            data-bs-toggle="modal" data-bs-target="#exampleModalCenter">QR</button>
          </div>
        </div>
        <button @click="toggleStudentList" class="btn btn-outline-success me-2" type="button">
          학생리스트
        </button>
      </form>
    </div>
  </nav>

  <!-- 클래스룸 본문 -->
  <div id="classBody">
    <Whiteboard :classCode="classCode" :sender="sender" />
    <!-- Other components can be added here -->
    
  </div>



  <!-- Student List Modal -->
  <div v-if="showStudentListVisible" class="modal fade show" tabindex="-1" aria-modal="true" role="dialog" style="display: block;">
    <div class="modal-dialog modal-dialog-scrollable">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">접속 중인 학생 리스트</h5>
          <button type="button" class="btn-close" @click="toggleStudentList"></button>
        </div>
        <div class="modal-body">
          <ul>
            <li v-for="(name, sessionId) in students" :key="sessionId">{{ name }}</li>
          </ul>
        </div>
      </div>
    </div>
  </div>

  <DimModal :modalData="modalData"/>


</template>

<script>
import Whiteboard from "../components/Whiteboard.vue";
import { mapState } from "vuex";
import { reactive } from "vue";
import DimModal from "../components/DimModal.vue";

export default {
  name: "Classroom",
  components: {
    Whiteboard,
    DimModal,
  },
  props: {
    classCode: {
      type: String,
      required: true,
    },
  },
  setup() {
    const students = reactive({});
    const modalData = reactive({ modalTitle: '', modalBody: '' });
    return { students, modalData };
  },
  data() {
    return {
      sender: this.$route.query.currentUser,
      showStudentListVisible: false,
      canLeaveSite: false,
    };
  },
  computed: {
    ...mapState(["socket"]),
  },
  mounted() {
    this.$store.dispatch("subscribeToClass", this.classCode);

    this.$store.subscribe((mutation, state) => {
      if (mutation.type === "addJoin") {
        this.handleStudentListJoin(mutation.payload);
      } else if (mutation.type === "addLeave") {
        this.handleStudentListLeave(mutation.payload);
      }
    });
    window.addEventListener("beforeunload", this.unLoadEvent);
  },
  methods: {
    unLoadEvent(event) {
      if (this.canLeaveSite) return;

      event.preventDefault();
      event.returnValue = "";

      this.leaveClassroom();
    },
    leaveClassroom() {
      const message = JSON.stringify({
        sender: this.sender,
        type: "LEAVE",
      });
      if (this.socket && this.socket.connected) {
        this.socket.publish({
          destination: `/pub/leave/${this.classCode}`,
          body: message,
        });
      }
    },
    handleStudentListJoin(message) {
      try {
        console.log("handleStudentListJoin", message);
        const { sender, sessionId } = message;
        this.students[sessionId] = sender;
      } catch (error) {
        console.error(error);
      }
    },
    handleStudentListLeave(message) {
      try {
        console.log("handleStudentListLeave", message);
        const { sessionId } = message;
        delete this.students[sessionId];
      } catch (error) {
        console.error(error);
      }
    },
    toggleStudentList() {
      this.showStudentListVisible = !this.showStudentListVisible;
    },
    changeModalData(title) {
      this.modalData.modalTitle = title;
      this.modalData.modalBody = this.classCode;
    },

  },
};
</script>

<style scoped>
#classBody {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.popup {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 200px;
  padding: 10px;
  background: white;
  border: 1px solid #ccc;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
</style>
