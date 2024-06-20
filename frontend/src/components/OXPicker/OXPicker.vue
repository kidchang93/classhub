<template>
  <div class="container">
    <div class="question-container">
      <label for="question"><h2>Q.</h2></label>
      <input type="text" id="question" v-model="question" placeholder="원하시는 경우, 질문을 입력하세요(선택)" />
    </div>

    <div class="choice-container">
      <div class="choice-button-container">
        <button class="choice-button">
          <div class="circle"></div>
        </button>
      </div>
      <div class="choice-button-container">
        <button class="choice-button">
          <div class="cross"></div>
        </button>
      </div>
    </div>

    <div class="action-container">
      <button @click="savePicker" class="action-button">저장하기</button>
      <button @click="this.$emit('startPicker', this.question)" class="action-button start-button">시작하기</button>
    </div>

    <button class="store-button" @click="this.$emit('switchComponent', 'PickerBox')">보관함</button>
  </div>
</template>

<script>
import axios from 'axios';
import {mapState} from "vuex";

export default {
  name: 'OXPicker',
  props: {
    classCode: {
      type: String,
      required: true,
    },
    sender: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      question: '',
    };
  },
  computed: {
    ...mapState(["socket"]),
  },
  methods: {
    selectChoice(choice) {
      this.choice = choice;
    },
    savePicker() {
      // Implement the logic for saving the selection
      const payload = {
        question: this.question,
        choices: [],
        type: 0,
        classroomId: 1
      };

      axios.post('http://localhost:8080/api/picker/save', payload)
        .then(response => {
          if (response.status === 201) {
            alert(`Saved: Question: ${this.question}`);
            this.$emit('switchComponent', 'PickerBox')
          } else {
            alert('Failed to save the question');
          }
        })
        .catch(error => {
          alert('Error saving the question');
        });

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
  position: relative;
}

.question-container {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

#question {
  margin-left: 10px;
  padding: 15px;
  border: 1px solid #ccc;
  border-radius: 4px;
  width: 700px;
  font-size: 1.5em;
}

.choice-container {
  display: flex;
  justify-content: space-between;
  width: 600px;
  margin-top: 50px;
  margin-bottom: 30px;
}

.choice-button-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.choice-button {
  width: 200px;
  height: 200px;
  border: none;
  background: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.circle {
  width: 200px;
  height: 200px;
  background-color: blue;
  border-radius: 50%;
}

.cross {
  width: 200px;
  height: 200px;
  position: relative;
}

.cross:before, .cross:after {
  content: '';
  position: absolute;
  width: 200px;
  height: 40px;
  background-color: red;
  top: 80px;
  left: 0;
}

.cross:before {
  transform: rotate(45deg);
}

.cross:after {
  transform: rotate(-45deg);
}

.choice-button.selected .circle {
  background-color: lightblue;
}

.choice-button.selected .cross:before,
.choice-button.selected .cross:after {
  background-color: lightcoral;
}

.action-container {
  display: flex;
  justify-content: space-between;
  width: 400px;
  margin-top: 60px;
  margin-bottom: 30px;
}

.action-button {
  padding: 20px 40px;
  border: 1px solid #ccc;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1em;
  font-weight: bold;
}
.start-button{
background-color: skyblue;
}

.store-button {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 20px 40px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #f0f0f0;
  cursor: pointer;
  font-size: 1.1em;
}
</style>