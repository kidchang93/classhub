<template>
  <div class="container">
    <div class="question-container">
      <label for="question"><h2>Q.</h2></label>
      <input type="text" id="question" v-model="question" placeholder="질문을 입력하세요" />
    </div>

    <div class="choice-container">
      <button class="choice-button" :class="{ selected: choice === 'O' }" @click="selectChoice('O')">
        <div class="circle"></div>
      </button>
      <button class="choice-button" :class="{ selected: choice === 'X' }" @click="selectChoice('X')">
        <div class="cross"></div>
      </button>
    </div>

    <div class="action-container">
      <button @click="startSelection" class="action-button start-button">시작하기</button>
      <button @click="saveSelection" class="action-button">저장하기</button>
    </div>

    <button class="store-button">보관함</button>
  </div>
</template>

<script>
export default {
  name: 'OXSelector',
  data() {
    return {
      question: '',
      choice: ''
    };
  },
  methods: {
    switchToComponentB() {
      this.$emit('switchComponent', 'ComponentB');
    },
    selectChoice(choice) {
      this.choice = choice;
    },
    startSelection() {
      // Implement the logic for starting selection
      alert('Selection started!');
      this.switchToComponentB();
    },
    saveSelection() {
      // Implement the logic for saving the selection
      alert(`Saved: Question: ${this.question}, Choice: ${this.choice}`);
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
  width: 400px;
  margin-top: 50px;
  margin-bottom: 30px;
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
  width: 160px;
  height: 160px;
  background-color: blue;
  border-radius: 50%;
}

.cross {
  width: 160px;
  height: 160px;
  position: relative;
}

.cross:before, .cross:after {
  content: '';
  position: absolute;
  width: 160px;
  height: 40px;
  background-color: red;
  top: 60px;
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