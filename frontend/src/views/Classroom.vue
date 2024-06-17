<template>
  <div id="classroom">
    <h2>Classroom Code: {{ classCode }}</h2>
    <button @click="toggleStudentList">학생 리스트 보기</button>
    <Whiteboard :classCode="classCode" :sender="sender" />

    <!-- 수진 -->



    <!-- 창규 -->



    <!-- 영주 -->



  </div>
  <div v-if="showStudentList" class="popup">
    <h3>접속 중인 학생 리스트</h3>
    <ul>
      <li v-for="(name, sessionId) in students" :key="sessionId">{{ name }}</li>
    </ul>
    <button @click="toggleStudentList">닫기</button>
  </div>
</template>

<script>
import Whiteboard from "../components/Whiteboard.vue";
import { mapState } from "vuex";
import { reactive } from "vue";

export default {
  name: "Classroom",
  components: {
    Whiteboard,
  },
  props: {
    classCode: {
      type: String,
      required: true,
    },
  },
  setup() {
    const students = reactive({});
    return { students };
  },
  data() {
    return {
      sender: this.$route.query.currentUser,
      showStudentList: false,
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
      this.showStudentList = !this.showStudentList;
    },
  },
};
</script>

<style scoped>
#classroom {
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
