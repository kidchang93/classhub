<template>
  <div class="modal-container" v-if="isWidgetModalOpen">
    <div class="modal-window">
      <div class="modal-header">
        <span class="modal-title">{{ title }}</span>
        <button class="close-button" @click="closeModal">X</button>
      </div>
      <div class="modal-body">
        <component :is="currentComponent" @switchComponent="switchComponent" />
      </div>
    </div>
  </div>
</template>

<script>
import OXSelector from './OXSelector.vue';
import ComponentB from './ComponentB.vue';

export default {
  name: 'WidgetModal',
  components: {
    OXSelector,
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
    }
  },
  data() {
    return {
      currentComponent: 'OXSelector'
    };
  },
  methods: {
    closeModal() {
      this.$emit('close');
    },
    switchComponent(componentName) {
      this.currentComponent = componentName;
    }
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
