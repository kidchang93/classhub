<template>
  <div id="whiteboard">
    <canvas class="fabric"
        ref="canvas"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp">
    </canvas>
    <div id="designTool">
      <button @click="buttonErase">모두 지우기</button>
      <br>
      <label for="drawing-line-width">Line width : </label>
      <span class="info">{{ lineWidth }}</span>
      <input type="range" :value="lineWidth" min="0" max="100" id="drawing-line-width" ref="drawingLineWidthEl" @change="changLineWidth"><br>

      <label for="drawing-color">Line color : </label>
      <span class="info">{{ color }}</span>
      <input type="color" :value="color" id="drawing-color" ref="drawingColorEl" @change="changeColor"><br>

      <button type="button" id="togglePen" ref="toggleButtonEl" @click="toggleDrawMode">그리기</button><br>
      <button type="button" id="drawing-rect" ref="drawingRectEl" @click="toggleRectMode">사각형 추가</button><br>
      <button type="button" id="drawing-rect" ref="drawingRectEl" @click="toggleArcMode">원 추가</button><br>
      <button type="button" id="clickObject" @click="clickObject">객체 선택</button><br>

    </div>
  </div>

</template>

<script>
import { mapState } from "vuex";
import {fabric} from "fabric";
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

      // fabric
      mode:'',
      brush:'',
      //
      canvas: null,
      drawing: false,
      context: null,
      prevX: 0,
      prevY: 0,
      lastX: 0,
      lastY: 0,
      scaleFactorX: 1,
      scaleFactorY: 1,
      // 화면 속성 변수
      width: 1850,
      height: 837,
      mouseX: 0,
      mouseY: 0,
      rect:{},
      // 캔버스 모드 변수
      drawMode:false,
      rectMode:false,
      arcMode: false,
      // 두 수의 차이
      xMinusX:0,
      yMinusY:0,
      // 속성 변수
      strokeStyle: '',
      lineWidth: 10,
      color: '#ffffff',
      lineCap: 'round',
      // 사각형 변수
      rectStartX: 0,
      rectStartY: 0,
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
    // window.addEventListener("resize", this.resizeCanvas);
  },
  beforeUnmount() {
    // window.removeEventListener("resize", this.resizeCanvas);
  },
  methods: {
    // 캔버스 세팅
    initCanvas() {

      this.canvas = new fabric.Canvas(this.$refs.canvas, {
        width: 1850,
        height: 837,
        backgroundColor: '#043e1a',
        isDrawingMode: true,
        brush: fabric.freeDrawingBrush,
      });
      this.brush = fabric.freeDrawingBrush;
      this.context = this.canvas.getContext("2d");
      this.rect = this.$refs.canvas.getBoundingClientRect();
      this.canvas.width = this.width;
      this.canvas.height = this.height;
      this.context.strokeStyle = this.color;
      this.context.lineWidth = this.lineWidth;
      this.context.lineCap = this.lineCap;

      console.log("initCanvas", this.rect);
    },


    // 선 굵기 변경
    changLineWidth(e) {
      this.lineWidth = e.target.value;

      console.log("선 굵기 : ",this.lineWidth);

    },
    // 선 색상 변경
    changeColor(e){
      this.color = e.target.value;
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

    // 모드에 따라 변하는 event속성
    handleMouseDown(e) {
      // 드로잉 모드시
      if (this.drawMode) {
        this.drawing = true;
        this.lastX = e.clientX - this.rect.left;
        this.lastY = e.clientY - this.rect.top;
      } else if (this.rectMode){
        // 사각형 모드시
        this.rectStartX = e.clientX - this.rect.left;
        this.rectStartY = e.clientY - this.rect.top;
        this.drawing = true;
      } else if (this.arcMode){
        // 원 모드시
      }
      console.log("현재 모드 : ",this.drawing);
    },
    handleMouseMove(e){
      if (!this.drawing) return;

        // 드로잉 모드일 때
      if (this.drawMode) {

        const newX = e.clientX - this.rect.left;
        const newY = e.clientY - this.rect.top;
        // 좌표 업데이트

        this.prevX = this.lastX;
        this.prevY = this.lastY;
        this.context.beginPath();
        this.context.moveTo(this.lastX, this.lastY);
        this.context.lineTo(newX, newY);

        this.context.lineWidth = this.lineWidth;
        this.context.strokeStyle = this.color;
        this.context.stroke();

        this.lastX = newX;
        this.lastY = newY;

      } else if (this.rectMode){
       // 사각형 모드일 때
        const x = e.clientX - this.rect.left;
        const y = e.clientY - this.rect.top;
        // 드로잉 될때마다 초기화
        this.context.clearRect(0,0, this.width, this.height);
        // 사각형 그리기
        this.context.strokeRect(this.rectStartX, this.rectStartY, x - this.rectStartX, y - this.rectStartY);

        this.context.strokeStyle = this.color;
        this.context.lineWidth = this.lineWidth;
        this.context.color = this.color;

      } else if (this.arcMode){
        // 원 그리기 모드일 때


      }
      // 메시지 전송
      const message = JSON.stringify({
        type: "DRAW",
        sender: this.sender,
        data: {
          x: this.lastX,
          y: this.lastY,
          prevX : this.prevX,
          prevY : this.prevY,
          color: this.color,
          lineWidth: this.lineWidth,
        },
      });
      if (this.socket && this.socket.connected) {
        this.socket.publish({
          destination: `/pub/update/${this.classCode}`,
          body: message,
        });
      }
    },

    // 그리기 스탑
    handleMouseUp() {
      this.drawing = false;
    },

    // 지우개 버튼
    buttonErase() {
      this.context.clearRect(0, 0, this.$refs.canvas.width, this.$refs.canvas.height);
    },
    toggleDrawMode() {
      this.drawMode = true;
      this.rectMode = false;
    },
    toggleRectMode() {
      this.drawMode = false;
      this.rectMode = true;
    },
    clickObject(){
      this.drawMode = false;
      this.rectMode = false;
    },

    // 메시지 수신 처리 함수
    handleIncomingDrawing(message) {
      const { type, data } = message;
      if (type === "DRAW") {
        const { x, y, prevX, prevY, color, lineWidth } = data;
        console.log(`보낸 메세지 (${prevX}, ${prevY}) to (${x}, ${y}) & (${color}) & (${lineWidth})`);
      }
    },
  },
};
</script>

<style scoped>
#whiteboard {
  position: relative;
  background: #043e1a;
}
.fabric {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
canvas {
  display: block;
}

#designTool {
  color: black;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid black;
  position: absolute;
  display: flex;
  flex-direction: column;
  padding: 20px;
  top: 20%;
  left: 85%;
}
</style>
