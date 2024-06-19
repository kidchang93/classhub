<template>
  <div>
    <h2>Component B</h2>
    <button @click="switchToComponentA">Switch to OXPicker</button>
  </div>
</template>

<script>
export default {
  name: 'ComponentB',
  methods: {
    switchToComponentA() {
      this.$emit('switchComponent', 'OXPicker');
    }
  }
};
</script>
