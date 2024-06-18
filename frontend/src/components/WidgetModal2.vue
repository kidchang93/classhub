<template>
  <div class="modal-container" v-if="isWidgetModalOpen">
    <div class="modal-window">
      <div class="modal-header">
        <span class="modal-title">{{ title }}</span>
        <button class="close-button" @click="closeModal">X</button>
      </div>
      <div class="modal-body">
        <slot></slot>
      </div>
    </div>
  </div>

</template>

<script>
export default {
  name: 'WidgetModal',
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
  methods: {
    closeModal() {
      this.$emit('close');
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
  height: 400px;
  width: 700px;
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
}
</style>