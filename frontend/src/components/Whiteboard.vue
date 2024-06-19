<template>
  <div id="whiteboard">
<!--    <canvas class="fabric"-->
<!--        ref="canvas"-->
<!--        @mousedown="handleMouseDown"-->
<!--        @mousemove="handleMouseMove"-->
<!--        @mouseup="handleMouseUp">-->
<!--    </canvas>-->
    <canvas class="fabric" ref="canvas"></canvas>

    <div id="designTool">
      <button type="button" class="btn btn-secondary" @click="buttonErase">모두 지우기</button>
      <br>
      <label for="drawing-line-width">Line width : </label>
      <span class="info">{{ lineWidth }}</span>
      <input type="range" :value="lineWidth" min="0" max="100" id="drawing-line-width" ref="drawingLineWidthEl" @change="changLineWidth"><br>

      <label for="drawing-color">그리기 색 : </label>
      <span class="info">{{ color }}</span>
      <input type="color" :value="color" id="drawing-color" ref="drawingColorEl" @change="changeColor"><br>

      <label for="drawing-color">도형 색 : </label>
      <span class="info">{{ fillColor }}</span>
      <input type="color" :value="fillColor" id="fill-color" ref="drawingFillColorEl" @change="changeFillColor"><br>

      <button type="button"
              class="btn btn-secondary"
              data-bs-toggle="button"
              @click="toggleDrawMode">그리기
      </button>
      <br>
      <!-- Default dropstart button -->
      <div class="btn-group dropstart">
        <button type="button"
                class="btn btn-secondary dropdown-toggle"
                data-bs-toggle="dropdown"
                aria-expanded="false">
          도형 추가
        </button>
        <ul class="dropdown-menu" >
          <!-- Dropdown menu links -->
          <li><a class="dropdown-item" @click="toggleMode('rect')">사각형</a></li>
          <li><a class="dropdown-item" @click="toggleMode">원</a></li>
        </ul>
      </div>
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
      x:0,
      y:0,
      lineCap:'round',
      fillColor:'#ffffff',
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

      // 두 수의 차이
      xMinusX:0,
      yMinusY:0,
      // 속성 변수
      strokeStyle: '',
      lineWidth: 10,
      color: '#ffffff',
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
    // this.$store.watch(
    //   (state) => state.events.length,
    //   (newLength) => {
    //     const event = this.events[newLength - 1];
    //     if (event) {
    //       this.handleIncomingDrawing(event);
    //       console.log(event)
    //     }
    //   }
    // );
    // window.addEventListener("resize", this.resizeCanvas);
  },
  beforeUnmount() {
    // window.removeEventListener("resize", this.resizeCanvas);
  },
  methods: {
    // 캔버스 세팅
    initCanvas() {

      this.canvas = new fabric.Canvas(this.$refs.canvas, {
        width: 1920,
        height: 1080,
        backgroundColor: '#043e1a',

      });
      // 이벤트 리스너 추가
      this.canvas.on('mouse:down', this.handleMouseDown)
      this.canvas.on('mouse:move', this.handleMouseMove)
      this.canvas.on('mouse:up', this.handleMouseUp)


      console.log("initCanvas", this.canvas.evented);

    },


    // 선 굵기 변경
    changLineWidth(e) {
      this.lineWidth = e.target.value;
      this.canvas.freeDrawingBrush.width = this.lineWidth;
      console.log("선 굵기 : ",this.canvas.freeDrawingBrush.width);

    },
    // 선 색상 변경
    changeColor(e){
      this.color = e.target.value;
      this.canvas.color = this.color;
      console.log('색깔 : ', this.canvas.color);
    },
    changeFillColor(e){
      this.fillColor = e.target.value;
      this.rect.color = this.fillColor;
      console.log(this.rect.color)
    },
    // brush 선택시 마우스다운 핸들러 들어갔을 때 수행하는 함수
    onBrush(event){
      // 포인터를 통해 현재 x,y 좌표 구하기.
      let brush = this.canvas.freeDrawingBrush;
      brush.width = parseInt(this.lineWidth) || 1

      this.canvas.isDrawingMode = true;
      const pointer = this.canvas.getPointer(event.e);
      this.x = pointer.x;
      this.y = pointer.y;

      this.canvas.freeDrawingBrush.width = this.lineWidth;
      this.canvas.color = this.color;

      console.log('x좌표 : ' ,this.x);
      console.log('y좌표 : ' ,this.y);
      console.log('lineWidth : ' ,this.canvas.freeDrawingBrush.width);
      console.log('color : ' ,this.canvas.color);
    },
    moveBrush(event){
      // 포인터를 통해 현재 x,y 좌표 구하기.

      const pointer = this.canvas.getPointer(event.e);

      let brush = this.canvas.freeDrawingBrush;
      brush.width = parseInt(this.lineWidth) || 1

      const newLine = new fabric.Line([this.x, this.y, pointer.x, pointer.y], {
        stroke: this.color,
        strokeWidth: brush.width,
        strokeLineCap: this.lineCap,
        selectable: false,
        event:false,
      });
      this.canvas.add(newLine);

      this.x = pointer.x;
      this.y = pointer.y;
      this.prevX = this.x;
      this.prevY = this.y;
      console.log("mouseMove : ", brush);

    },
    // 이벤트 핸들러 : handleMouseDown
    handleMouseDown(event) {

      // 드로잉 모드가 활성화 됐을 때는 모드가 brush 이고
      // 드로잉 모드가 비활성화 됐을 때는 모드가 객체 선택 모드이다. 라는 로직으로 가야될듯?



      this.onBrush(event)

      // this.selectRect(event)

     //  if (this.mode == 'rect') {
     //   this.canvas.isDrawingMode = false;
     //   this.mode = 'rect'
     //   this.selectRect(event)
     //
     //   console.log("렉트 왔냐 ", this.mode)
     // }
      console.log("현재 모드 : ",this.mode);
    },
    handleMouseMove(event){

        // 드로잉 모드일 때
        if ( this.canvas.isDrawingMode && this.mode == 'brush' ) {
          this.moveBrush(event)
        } else if (this.mode == 'rect' ) {

          return
      }
      // 메시지 전송
      const message = JSON.stringify({
        type: "DRAW",
        sender: this.sender,
        data: {
          x: this.x,
          y: this.y,
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
    selectRect(event){
      // this.canvas.isDrawingMode = false;
      this.mode = 'rect';

      const selectRect = event.target;
      console.log(selectRect)
    },
    // 사각형 도형 추가 버튼
    addRect(){

      const rect = new fabric.Rect({
        // top: Math.random() * this.canvas.height,
        // left: Math.random() * this.canvas.width,
        top: 100,
        left: 100,
        width: 500,
        height: 500,
        fill: this.fillColor,
        corner: 100,
        angle: 360,
        borderColor: "#3845ff",
        cornerColor: "#ffc100",
        cornerSize: 20,
        transparentCorners: false,
        evented: true,
        event: true,
          }
      )

      this.canvas.add(rect);
      this.canvas.setActiveObject(rect);
      this.canvas.renderAll();
      console.log("rect", rect);
    },
    // 그리기 스탑
    handleMouseUp() {
      this.canvas.isDrawingMode = false;

      console.log("stop")
    },

    // 전체삭제 버튼
    buttonErase() {
      this.canvas.clear();
    },
    // 그리기 버튼
    toggleDrawMode() {
      this.mode = 'brush';
      this.drawing = true;
      // this.canvas.isDrawingMode = true;
      console.log(this.drawing)
    },
    toggleMode(mode) {
      this.canvas.isDrawingMode = false;
      if (mode == 'rect'){
        this.mode = 'rect'
        console.log(this.mode)
        this.addRect()

      } else if(this.mode == 'arc') {

      }
    },
    clickObject(){
      this.canvas.isDrawingMode = false;
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
