<template>
  <div id="home">
    <h1>Welcome to the Classroom</h1>
    <button @click="goToTeacherLogin">선생님 입장</button>
    <button @click="goToStudentLogin">학생 입장</button>
  </div>
</template>

<script>
export default {
  name: "Home",
  methods: {
    goToTeacherLogin() {
      this.$router.push({ name: "TeacherLogin" });
    },
    goToStudentLogin() {
      this.$router.push({ name: "Student" });
    },
  },
};
</script>

<style scoped>
#home {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 50px;
}
button {
  margin: 10px;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
}
</style>
