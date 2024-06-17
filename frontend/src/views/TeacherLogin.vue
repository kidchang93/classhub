<template>
  <div id="teacher-login">
    <form @submit.prevent="login">
      <label for="username">Username:</label>
      <input v-model="username" type="text" id="username" required />
      <label for="password">Password:</label>
      <input v-model="password" type="password" id="password" required />
      <button type="submit">Login</button>
    </form>
  </div>
</template>

<script>
import axios from "axios";
import { mapState } from "vuex";

export default {
  data() {
    return {
      username: "",
      password: "",
      error: null,
    };
  },
  computed: {
    ...mapState(["socket"]),
  },
  methods: {
    login() {
      axios
        .post("http://localhost:8080/api/teacher/login", {
          username: this.username,
          password: this.password,
        })
        .then((response) => {
          if (response.data) {
            return axios.get(
              `http://localhost:8080/api/classrooms/teacher/${response.data}`
            );
          } else {
            throw new Error("Invalid credentials");
          }
        })
        .then((classCodeResponse) => {
          const classCode = classCodeResponse.data.code;
          // WebSocket 연결 초기화
          const connect = this.$store.dispatch("initializeWebSocket", this.classCode);
          connect.then(() => {
            // Redirect to the classroom page
            this.$router.push({
              name: "Classroom",
              params: { classCode },
              query: { currentUser: this.username },
            });
          });
        })
        .catch((error) => {
          console.error("Login failed: ", error.message);
          alert("Login failed: " + error.message);
        });
    },
  },
};
</script>

<style scoped>
#teacher-login {
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
