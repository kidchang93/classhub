<template>
  <div class="modal-container" v-if="isWidgetModalOpen">
    <div class="modal-window">
      <div class="modal-header">
        <span class="modal-title">{{ title }}</span>
        <button class="close-button" @click="thisModalOFF">X</button>
      </div>
      <div class="modal-body">
        <component :is="currentComponent"
                   @switchComponent="switchComponent" @startPicker="startPicker" @endPicker="endPicker" @toggleWidgetModal="toggleWidgetModal"
                   :classCode="classCode" :sender="sender" :message="this.message"/>
      </div>
    </div>
  </div>
</template>

<script>
import OXPicker from './OXPicker/OXPicker.vue';
import PickerBox from './OXPicker/PickerBox.vue';
import ComponentB from './ComponentB.vue';
import Whiteboard from "./Whiteboard.vue";
import OXPickerSelect from "./OXPicker/OXPickerSelect.vue";
import OXPickerResult from "./OXPicker/OXPickerResult.vue";
import {mapState} from "vuex";

export default {
  name: 'WidgetModal2',
  components: {
    Whiteboard,
    OXPicker,
    PickerBox,
    OXPickerSelect,
    OXPickerResult,
    ComponentB
  },
  props: {
    isWidgetModalOpen: {
      type: Boolean,
      required: true
    },
    title: {
      type: String,
      default: 'Modal Title2'
    },
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
      currentComponent: 'OXPicker',
      message:{}
    };
  },
  computed: {
    ...mapState(["socket", "pickerStarts", "pickerEnds"]),
  },
  mounted() {
    this.$store.watch(
        (state) => state.pickerStarts.length,
        (newLength) => {
          const event = this.pickerStarts[newLength - 1];
          if (event) {
            this.handlePickerStart(event);
          }
        }
    );
    this.$store.watch(
        (state) => state.pickerEnds.length,
        (newLength) => {
          const event = this.pickerEnds[newLength - 1];
          if (event) {
            this.handlePickerEnd(event);
          }
        }
    );
  },
  methods: {
    thisModalOFF(){
      this.$emit('toggleWidgetModal');
      this.endPicker();
      this.switchComponent('OXPicker');
    },
    toggleWidgetModal() {
      this.$emit('toggleWidgetModal');
    },
    switchComponent(componentName) {
      this.currentComponent = componentName;
    },
    handlePickerStart(message) {
      // Handle picker start event for students
      this.message = message;
      this.switchComponent('OXPickerSelect');
      this.toggleWidgetModal();
      console.log("Picker start event received:", message);
    },
    handlePickerEnd(){
      // Handle picker end event for students
      this.toggleWidgetModal();
    },
    startPicker(question) {
      // Implement the logic for starting selection for teacher
      alert('Selection started!');
      // this.switchToComponentB();
      // 메시지 전송
      const message = JSON.stringify({
        type: "PICKER/START",
        sender: this.sender,
        data: {
          question: question,
        },
      });
      if (this.socket && this.socket.connected) {
        this.socket.publish({
          destination: `/pub/picker/start/${this.classCode}`,
          body: message,
        });
      }
      this.switchComponent('OXPickerResult');
    },
    endPicker() {
      // Implement the logic for starting selection for teacher
      alert('Selection end!');
      // this.switchToComponentB();
      // 메시지 전송
      const message = JSON.stringify({
        type: "PICKER/END",
        sender: this.sender,
        data: {
        },
      });
      if (this.socket && this.socket.connected) {
        this.socket.publish({
          destination: `/pub/picker/end/${this.classCode}`,
          body: message,
        });
      }
    },
  }
};
</script>

<style scoped>
.modal-container {
  position: fixed;
  top: 100px;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  pointer-events: none;
}

.modal-window {
  background: white;
  border-radius: 8px;
  width: 1200px;
  height: 700px;
  pointer-events: all;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal-header {
  background: lightgray;
  padding: 10px;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  text-align: center;
  flex-grow: 1;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.2em;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
  height: 80%;
}
</style>
