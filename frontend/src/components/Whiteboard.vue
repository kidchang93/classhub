<template>
  <div id="whiteboard">
    <canvas
      ref="canvas"
      width="800"
      height="800"
      @mousedown="startDrawing"
      @mousemove="draw"
      @mouseup="stopDrawing"
      @mouseout="stopDrawing"
    ></canvas>
    <div id="designTool">
      <button @click="buttonErase">모두 지우기</button>
      <button @click="increaseThickness">두껍게</button>
      <button @click="decreaseThickness">얇게</button>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";

export default {
  name: "Whiteboard",
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
      drawing: false,
      context: null,
      lastX: 0,
      lastY: 0,
      scaleFactorX: 1,
      scaleFactorY: 1,
    };
  },
  computed: {
    ...mapState(["socket", "events"]),
  },
  mounted() {
    this.initCanvas();
    this.$store.watch(
      (state) => state.events.length,
      (newLength) => {
        const event = this.events[newLength - 1];
        if (event) {
          this.handleIncomingDrawing(event);
        }
      }
    );
    window.addEventListener("resize", this.resizeCanvas);
  },
  beforeUnmount() {
    window.removeEventListener("resize", this.resizeCanvas);
  },
  methods: {
    initCanvas() {
      this.resizeCanvas();
      const canvas = this.$refs.canvas;
      this.context = canvas.getContext("2d");
      this.context.strokeStyle = "#000000";
      this.context.lineWidth = 10;
      this.context.lineCap = "round";
      console.log("initCanvas");
    },
    resizeCanvas() {
      const canvas = this.$refs.canvas;
      const rect = canvas.getBoundingClientRect();
      const dpr = window.devicePixelRatio || 1;

      canvas.width = rect.width * dpr;
      canvas.height = rect.height * dpr;

      this.scaleFactorX = canvas.width / rect.width;
      this.scaleFactorY = canvas.height / rect.height;

      this.context = canvas.getContext("2d");
      this.context.scale(dpr, dpr);

      console.log("resizeCanvas", this.scaleFactorX, this.scaleFactorY);
    },
    startDrawing(e) {
      this.drawing = true;
      this.lastX = e.offsetX * this.scaleFactorX;
      this.lastY = e.offsetY * this.scaleFactorY;
      console.log("startDrawing");
    },
    draw(e) {
      if (!this.drawing) return;

      const newX = e.offsetX * this.scaleFactorX;
      const newY = e.offsetY * this.scaleFactorY;

      this.context.beginPath();
      this.context.moveTo(this.lastX, this.lastY);
      this.context.lineTo(newX, newY);
      this.context.stroke();

      const prevX = this.lastX;
      const prevY = this.lastY;
      this.lastX = newX;
      this.lastY = newY;

      console.log("draw", this.lastX, this.lastY);

      const message = JSON.stringify({
        type: "DRAW",
        sender: this.sender,
        data: {
          x: this.lastX,
          y: this.lastY,
          prevX,
          prevY,
        },
      });
      if (this.socket && this.socket.connected) {
        this.socket.publish({
          destination: `/pub/update/${this.classCode}`,
          body: message,
        });
      }
    },
    stopDrawing() {
      this.drawing = false;
    },
    buttonErase() {
      this.context.clearRect(0, 0, this.$refs.canvas.width, this.$refs.canvas.height);
    },
    increaseThickness() {
      this.context.lineWidth += 1;
      console.log("지금 두께 : ", this.context.lineWidth);
    },
    decreaseThickness() {
      if (this.context.lineWidth > 1) {
        this.context.lineWidth -= 1;
        console.log("지금 두께 : ", this.context.lineWidth);
      }
    },
    handleIncomingDrawing(event) {
      const { type, data } = event;
      if (type === "DRAW") {
        const { x, y, prevX, prevY } = data;
        console.log(
          `Handling incoming drawing from (${prevX}, ${prevY}) to (${x}, ${y})`
        );

        this.context.beginPath();
        this.context.moveTo(prevX, prevY);
        this.context.lineTo(x, y);
        this.context.stroke();

        this.lastX = x;
        this.lastY = y;
      }
    },
  },
};
</script>

<style scoped>
#whiteboard {
  position: relative;
  width: 100%;
  height: 70vh;
  background: green;
}
canvas {
  display: block;
  width: 100%;
  height: 100%;
}
#designTool {
  position: absolute;
  top: 10px;
  left: 10px;
}
</style>
