<template>
  <div class="container">
    <div class="question-container">
      <label for="question"><h2>Q.</h2></label>
      <input type="text" id="question" v-model="question" readonly/>
    </div>

    <div class="choice-container">
      <div class="choice-button-container">
        <button class="choice-button">
          <div class="circle"></div>
        </button>
        <div class="choice-stats">
          <div>{{ circleCount }}명</div>
          <div>{{ circlePercentage }}%</div>
        </div>
      </div>
      <div class="choice-button-container">
        <button class="choice-button">
          <div class="cross"></div>
        </button>
        <div class="choice-stats">
          <div>{{ crossCount }}명</div>
          <div>{{ crossPercentage }}%</div>
        </div>
      </div>
    </div>

    <div class="status-container">
      <div class="status-box">
        <div class="status-label">응답 진행 현황</div>
        <div class="status-value">{{ responseCount }} / {{ totalStudents }}</div>
      </div>
    </div>

    <div class="action-container">
      <button @click="this.$emit('switchComponent', 'OXPicker')" class="action-button">질문 생성으로 돌아가기</button>
      <button @click="endResult" class="action-button end-button">종료</button>
    </div>

  </div>
</template>

<script>
import axios from 'axios';
import { mapState } from "vuex";

export default {
  name: 'OXPickerResult',
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
      circleCount: 0,
      crossCount: 0,
      responseCount: 0,
      totalStudents: 21,
    };
  },
  computed: {
    ...mapState(["socket", "pickerSelects"]),
    circlePercentage() {
      return ((this.circleCount / this.totalStudents) * 100).toFixed(2);
    },
    crossPercentage() {
      return ((this.crossCount / this.totalStudents) * 100).toFixed(2);
    }
  },
  mounted() {
    this.$store.watch(
        (state) => state.pickerSelects.length,
        (newLength) => {
          const event = this.pickerSelects[newLength - 1];
          if (event) {
            this.handleIncomingSelect(event);
          }
        }
    );
  },
  methods: {
    handleIncomingSelect(message){

      const { sessionId, data } = message;
      if(data.choice === "O"){
        this.circleCount++
        this.responseCount++
      }else if(data.choice === "X"){
        this.crossCount++
        this.responseCount++
      }

    },
    endResult() {
      this.$emit('toggleWidgetModal')
      this.$emit('endPicker');
      this.$emit('switchComponent', 'OXPicker')
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

.choice-stats {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 10px;
  font-size: 1.2em;
}

.status-container {
  position: absolute;
  top: 0px;
  right: 20px;
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.status-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 4px;
}

.status-label {
  font-weight: bold;
  margin-bottom: 5px;
}

.status-value {
  font-size: 1.5em;
}

.action-container {
  display: flex;
  justify-content: center;
  width: 600px;
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


.end-button {
  background-color: lightcoral;
}

</style>
