<template>
  <div id="whiteboard">
    <canvas ref="canvas"></canvas>

    <div id="designTool">
      <button @click="clearCanvas">모두 지우기</button>
      <br>
      <label for="drawing-line-width">Line width : </label>
      <span class="info">{{ lineWidth }}</span>
      <input type="range" v-model="lineWidth" min="1" max="100" id="drawing-line-width"><br>

      <label for="drawing-color">Line color : </label>
      <span class="info">{{ color }}</span>
      <input type="color" v-model="color" id="drawing-color"><br>

      <button @click="toggleDrawMode">그리기 모드</button><br>
      <button @click="toggleRectMode">사각형 모드</button><br>
    </div>
  </div>
</template>

<script>
import {fabric} from "fabric";
import { mapState } from "vuex";
export default {
  name: "WhiteBoard2",
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
  data(){
    return {
      canvas: null,
      drawMode: false,
      rectMode: false,
      lineWidth: 10,
      color: '#000000',
    }
  },
  mounted() {
    this.initCanvas();
  },
  methods: {
    initCanvas() {
      this.canvas = new fabric.Canvas(this.$refs.canvas, {
        width: 1850,
        height: 837,
        backgroundColor: '#ffffff',
      });
    },
    toggleDrawMode() {
      this.drawMode = !this.drawMode;
      this.rectMode = false;
      if (this.drawMode) {
        this.canvas.isDrawingMode = true;
        this.canvas.freeDrawingBrush.width = this.lineWidth;
        this.canvas.freeDrawingBrush.color = this.color;
      } else {
        this.canvas.isDrawingMode = false;
      }
    },
    toggleRectMode() {
      this.rectMode = !this.rectMode;
      this.drawMode = false;
      this.canvas.isDrawingMode = false;
      if (this.rectMode) {
        this.canvas.off('mouse:down');
        this.canvas.on('mouse:down', (opt) => {
          const pointer = this.canvas.getPointer(opt.e);
          const rect = new fabric.Rect({
            left: pointer.x,
            top: pointer.y,
            fill: 'transparent',
            stroke: this.color,
            strokeWidth: this.lineWidth,
            selectable: true,
          });
          this.canvas.add(rect);
          this.canvas.setActiveObject(rect);
          rect.enterEditing();
        });
      } else {
        this.canvas.off('mouse:down');
      }
    },
    clearCanvas() {
      this.canvas.clear();
    },
  },
  watch: {
    lineWidth(newVal) {
      if (this.canvas.isDrawingMode) {
        this.canvas.freeDrawingBrush.width = newVal;
      }
    },
    color(newVal) {
      if (this.canvas.isDrawingMode) {
        this.canvas.freeDrawingBrush.color = newVal;
      }
    },
  }
}

</script>



<style scoped>

</style>