<template>
    <div class="container">
      <div class="question-container">
        <div v-for="(question, index) in questions" :key="question.id" class="question-item">
          <input type="radio" :value="index" v-model="selectedQuestion" />
          <span>{{ question.question }}</span>
          <button @click="editQuestion(question.id)" class="small-button">수정</button>
          <button @click="deleteQuestion(question.id)" class="small-button">삭제</button>
        </div>
        <div class="action-container">
          <button @click="this.$emit('switchComponent', 'OXPicker')" class="action-button">질문 생성으로 돌아가기</button>
          <button @click="startSelection" class="action-button start-button">시작하기</button>
        </div>
      </div>
    </div>
</template>
  
<script>
  import axios from 'axios';
  
  export default {
    data() {
      return {
        questions: [],
        selectedQuestion: null
      };
    },
    created() {
      this.fetchQuestions();
    },
    methods: {
      fetchQuestions() {
        axios.get('http://localhost:8080/api/picker/get-questions', {
          params: {
            classroomid: 1,
            type: 0
          }
        })
        .then(response => {
          this.questions = response.data;
        })
        .catch(error => {
          console.error('Error fetching questions:', error);
        });
      },
      editQuestion(id) {
        // Edit question logic with id
        const url = `http://localhost:8080/api/picker/edit-question?id=${id}`;
        // Implement edit logic here
        console.log(url);
      },
      deleteQuestion(id) {
        // Delete question logic with id
        const url = `http://localhost:8080/api/picker/delete-question?id=${id}`;
        // Implement delete logic here
        console.log(url);
      },
      startSelection() {
        // Start selection logic
      }
    }
  };
</script>
  
<style scoped>
  .container {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: 20px;
  }
  .question-container {
    width: 80%;
  }
  .question-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }
  .question-item span {
    flex-grow: 1;
    margin-left: 10px;
  }
  .question-item button {
    margin-left: 5px;
  }
  
  .small-button {
    padding: 10px 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    cursor: pointer;
  }
  
  .action-button {
    padding: 20px 40px;
    border: 1px solid #ccc;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1.1em;
    font-weight: bold;
  }
  
  .start-button {
    background-color: skyblue;
  }
  
  .action-container {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-top: 20px;
  }
</style>
  