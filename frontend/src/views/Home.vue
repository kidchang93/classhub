<template>
  <div class="container py-3">
    <header>
      <div class="d-flex flex-column flex-md-row align-items-center pb-3 mb-4 border-bottom">
        <a href="https://getbootstrap.com/" class="d-flex align-items-center link-body-emphasis text-decoration-none">
          <!-- Pen Icon from Bootstrap Icons -->
          <svg xmlns="http://www.w3.org/2000/svg" width="40" height="32" class="me-2" viewBox="0 0 16 16" role="img">
            <title>Pen</title>
            <path d="M13.498.795l.149-.149a1.207 1.207 0 0 1 1.707 1.707l-.149.149-1.708-1.707zM10.854 3.439L1.854 12.439V14.5a.5.5 0 0 0 .5.5h2.061l9-9-2.561-2.561zM9.732 2.317L3 9.05V11h1.95l6.732-6.732-1.95-1.95z"/>
          </svg>
          <span class="fs-4">판서</span>
        </a>

        <nav class="d-inline-flex mt-2 mt-md-0 ms-md-auto">
          <a class="me-3 py-2 link-body-emphasis text-decoration-none" @click="goToTeacherLogin">교사 로그인</a>
        </nav>
      </div>

      <div class="pricing-header p-3 pb-md-4 mx-auto text-center">
        <h1 class="display-4 fw-normal text-body-emphasis">판서</h1>
        <p class="fs-5 text-body-secondary">실시간 전자칠판</p>
      </div>
    </header>

    <main>
      <div class="row row-cols-1 row-cols-md-2 mb-2 text-center">
        <div class="col">
          <div class="card mb-4 rounded-3 shadow-sm">
            <div class="card-body">
              <h1 class="card-title pricing-card-title">선생님 수업하기</h1>
              <ul class="list-unstyled mt-3 mb-4">
                <li>T셀파 회원가입이 필요해요!</li>
              </ul>
              <button @click="goToTeacherLogin" type="button" class="w-50 btn btn-lg btn-outline-primary">수업하기</button>
            </div>
          </div>
        </div>
        <div class="col">
          <div class="card mb-4 rounded-3 shadow-sm">
            <div class="card-body">
              <h1 class="card-title pricing-card-title">학생 참여하기</h1>
              <ul class="list-unstyled mt-3 mb-4">
                <li>회원가입이 필요없어요!</li>
              </ul>
              <button @click="goToStudentLogin" type="button" class="w-50 btn btn-lg btn-outline-primary">참여하기</button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <footer class="pt-4 my-md-5 pt-md-5 border-top">
      <div class="row">
        <div class="col-12 col-md">
          <img class="mb-2" src="./Pricing example · Bootstrap v5.3_files/bootstrap-logo.svg" alt="" width="24" height="19">
          <small class="d-block mb-3 text-body-secondary">© 2017–2024</small>
        </div>
        <div class="col-6 col-md">
          <h5>Features</h5>
          <ul class="list-unstyled text-small">
            <li class="mb-1"><a class="link-secondary text-decoration-none" href="https://getbootstrap.com/docs/5.3/examples/pricing/#">Cool stuff</a></li>
          </ul>
        </div>
        <div class="col-6 col-md">
          <h5>Resources</h5>
          <ul class="list-unstyled text-small">
            <li class="mb-1"><a class="link-secondary text-decoration-none" href="https://getbootstrap.com/docs/5.3/examples/pricing/#">Resource</a></li>
          </ul>
        </div>
        <div class="col-6 col-md">
          <h5>About</h5>
          <ul class="list-unstyled text-small">
            <li class="mb-1"><a class="link-secondary text-decoration-none" href="https://getbootstrap.com/docs/5.3/examples/pricing/#">Team</a></li>
          </ul>
        </div>
      </div>
    </footer>
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
