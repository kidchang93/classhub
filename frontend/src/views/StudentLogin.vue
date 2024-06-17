<template>
  <div id="student-login">
    <form @submit.prevent="joinClassroom">
      <label for="classCode">Class Code:</label>
      <input v-model="classCode" type="text" id="classCode" required />
      <label for="studentName">Name:</label>
      <input v-model="studentName" type="text" id="studentName" required />
      <button type="submit">Join Classroom</button>
    </form>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  data() {
    return {
      classCode: "",
      studentName: "",
    };
  },
  computed: {
    ...mapState(["socket"]),
  },
  methods: {
    async joinClassroom() {
      if (this.classCode && this.studentName) {
        console.log("Joining classroom:", this.classCode, "with name:", this.studentName);

        try {
          // WebSocket 연결 초기화
          const connect = this.$store.dispatch("initializeWebSocket", this.classCode);

          connect.then(() => {
            this.socket.publish({
              destination: `/pub/join/${this.classCode}`,
              body: JSON.stringify({
                type: "JOIN",
                sender: this.studentName,
              }),
            });
            // Classroom 페이지로 이동
            this.$router.push({
              name: "Classroom",
              params: { classCode: this.classCode },
              query: { currentUser: this.studentName },
            });
          });
        } catch (error) {
          console.error("Failed to initialize WebSocket connection:", error);
          alert("Failed to initialize WebSocket connection. Please try again.");
        }
      } else {
        alert("Classroom ID and Name are required.");
      }
    },
  },
};
</script>

<style scoped>
#student-login {
  display: flex;
  flex-direction: column;
  width: 300px;
  margin: 50px auto;
}
label {
  margin: 10px 0 5px;
}
input {
  padding: 10px;
  margin-bottom: 15px;
  border: 1px solid #ccc;
}
button {
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  cursor: pointer;
}
button:hover {
  background-color: #0056b3;
}
</style>
