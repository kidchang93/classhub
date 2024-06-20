<template>
    <canvas class="fabric" ref="canvas"></canvas>

    <div id="designTool">
      <button type="button" class="btn btn-secondary" @click="buttonErase">모두 지우기</button>
      <br>
      <button type="button" class="btn btn-secondary" @click="eraser">지우개</button>
      <br>
      <label for="drawing-line-width">Line width : </label>
      <span class="info">{{ lineWidth }}</span>
      <input type="range" :value="lineWidth" min="0" max="100" id="drawing-line-width" ref="drawingLineWidthEl" @change="changeLineWidth"><br>

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
      <button type="button"
              class="btn btn-secondary"
              data-bs-toggle="button"
              @click="test">테스트
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
          <li><a class="dropdown-item" @click="toggleMode('rect')">정사각형</a></li>
          <li><a class="dropdown-item" @click="toggleMode('triangle')">정삼각형</a></li>
          <li><a class="dropdown-item" @click="toggleMode('circle')">원</a></li>
        </ul>
      </div>
      <br>
      <button type="button"
              class="btn btn-secondary"
              data-bs-toggle="button"
              @click="clickObject">객체 선택
      </button>
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
      // brush:'',
      x:0,
      y:0,
      lineCap:'round',
      fillColor:'#ffffff',
      pointer:{},
      draw:[],
      rect:{},
      triangle:{},
      circle:{},
      message:{},
      drawing: false,

      // 화면 속성 변수

      // 속성 변수
      lineWidth: 10,
      color: '#ffffff',
      // 사각형 변수

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
          console.log("watch 실행 : " ,event)
        }
      }
    );
  },
  methods: {
    // 캔버스 세팅
    initCanvas() {

      this.canvas = new fabric.Canvas(this.$refs.canvas, {
        width: 1920,
        height: 1080,
        backgroundColor: '#2c4332',
        isDrawingMode: false,

      });
      // 브러시 초기 설정
      this.canvas.freeDrawingBrush.color = this.color;
      this.canvas.freeDrawingBrush.width = this.lineWidth;      console.log("초기 설정 : ",this.canvas);
      // 이벤트 리스너 추가
      this.canvas.on('mouse:down', this.handleMouseDown)
      this.canvas.on('mouse:move', this.handleMouseMove)
      this.canvas.on('mouse:up', this.handleMouseUp)

      console.log("initCanvas");

    },
    handleMouseDown(e) {
      if (this.drawing) {
        this.canvas.isDrawingMode = true;
        // this.canvas.freeDrawingBrush.color = this.color;
        // this.canvas.freeDrawingBrush.width = this.lineWidth;
        this.isMouseDown = true;
      }
    },
    handleMouseMove(e) {
      if (this.drawing && this.isMouseDown) {
        // this.canvas.freeDrawingBrush.color = this.color;
        // this.canvas.freeDrawingBrush.width = this.lineWidth;

        console.log("마우스 무브")
      }

    },
    handleMouseUp(e) {

      this.drawing = false;
      this.isMouseDown = true;
      this.canvas.renderAll();
      console.log("마우스 업")
    },

    test(){
      // rect에 집어넣은 변수값으로 생성된다. 이게 되네...
      const newRect = new fabric.Rect(this.rect);
      this.canvas.add(newRect);
      console.log(newRect)
    },


    eraser(){
      this.drawing = false;

      console.log("지우개 모드 맞음? : ",this.canvas.freeDrawingBrush);
    },
    // 선 굵기 변경
    changeLineWidth(e) {
      this.lineWidth = e.target.value;
      this.canvas.freeDrawingBrush.width = this.lineWidth;
      console.log("선 굵기 : ",this.canvas.freeDrawingBrush.width);

    },
    // 선 색상 변경
    changeColor(e){
      this.color = e.target.value;
      this.canvas.freeDrawingBrush.color = this.color;
      console.log('색깔 : ', this.canvas.freeDrawingBrush.color);
    },
    changeFillColor(e){
      this.fillColor = e.target.value;
    },


    // 기존 이벤트 핸들러 제거 => initial 함수에서 조건에 맞는 함수로 event 보내기

    sendMessage(type, event){
      console.log("sendMessage : ",event)
      // Drawing 메시지 전송
      if (type == 'DRAW') {
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          data: {
            draw: this.draw,
            color: this.color,
            lineWidth: this.lineWidth,
          },
        });
        if (this.socket && this.socket.connected) {
          this.socket.publish({
            destination: `/pub/update/${this.classCode}`,
            body: this.message,
          });
        }
      }
      console.log("메세지 전송 : ", this.message)
    },

    // 사각형 도형 추가 버튼
    addRect(){

      const rect = new fabric.Rect({
        // top: Math.random() * this.canvas.height,
        // left: Math.random() * this.canvas.width,
        top: 100,
        left: 100,
        width: 300,
        height: 300,
        fill: this.fillColor,
        borderColor: "#98bed8",
        cornerColor: "#98bed8",
        cornerSize: 15,
        transparentCorners: false,
        hasControls: true,
        selectable: true,
        evented: true,
        }
      )
      this.rect = rect;
      this.canvas.add(rect);
      this.canvas.setActiveObject(rect);
      this.canvas.renderAll();
      this.sendMessage("rect",rect);
      this.canvas.on('object:added')
      console.log("rect", rect);
      console.log("도형 추가")
    },

    addCircle(){
      const circle = new fabric.Circle({
        top: 300,
        left: 300,
        radius: 100,
        fill: this.fillColor,
        borderColor: "#98bed8",
        cornerColor: "#98bed8",
        cornerSize: 15,
        transparentCorners: false,
        hasControls: true,
        selectable: true,
        evented: true,
        objectCaching: true,

      })
      this.circle = circle;
      this.canvas.add(circle);
      this.canvas.setActiveObject(circle);
      this.canvas.renderAll();
      this.sendMessage("circle",circle);
      this.canvas.on('object:added')
      console.log("circle", circle);
      console.log("도형 추가")
    },

    addTriangle(){
      const triangle = new fabric.Triangle({
        top: 100,
        left: 100,
        width: 300,
        height: 300,
        fill: this.fillColor,
        borderColor: "#98bed8",
        cornerColor: "#98bed8",
        cornerSize: 15,
        transparentCorners: false,
        hasControls: true,
        selectable: true,
        evented: true,
      })
      this.triangle = triangle;
      this.canvas.add(triangle);
      this.canvas.setActiveObject(triangle);
      this.canvas.renderAll();
      this.sendMessage("triangle",triangle);
      this.canvas.on('object:added')
      console.log("triangle", triangle);
      console.log("도형 추가")
    },
    // 객체 선택
    selectObject(e){

      const selectObject = this.canvas.getActiveObject();

      console.log("selectObject : ",selectObject);
      this.canvas.renderAll();
      this.sendMessage(selectObject);
    },
    // 전체삭제 버튼
    buttonErase() {
      this.canvas.clear();
      this.canvas.renderAll();
    },
    // 그리기 버튼
    toggleDrawMode() {
      this.mode = 'brush';
      this.drawing = true;
      this.canvas.isDrawingMode = true;
      this.canvas.freeDrawingBrush.color = this.color;
      this.canvas.freeDrawingBrush.lineWidth = this.lineWidth;

    },
    // 도형 추가 버튼
    toggleMode(mode) {

      if (mode == 'rect'){
        this.canvas.isDrawingMode = false;
        this.drawing = false;
        this.mode = 'rect'
        console.log(this.mode)
        this.addRect()

      } else if(mode == 'circle') {
        this.canvas.isDrawingMode = false;
        this.drawing = false;
        this.mode = 'circle'
        console.log(this.mode)
        this.addCircle()
      } else if(mode == 'triangle'){
        this.canvas.isDrawingMode = false;
        this.drawing = false;
        this.mode = 'circle'
        console.log(this.mode)
        this.addTriangle()
      }
    },
    clickObject(){
      this.canvas.isDrawingMode = false;
      this.drawing = false;
    },

    // 메시지 수신 처리 함수
    handleIncomingDrawing(message) {
      const { type, data } = message;
      if (type === "DRAW") {
        console.log("draw 들어옴? : ",data);
        // const { x, y, prevX, prevY, color, fillColor, lineWidth, rect, triangle, circle } = data;
        // const { [x1, x2, y1, y2] color, lineWidth } = data;
        const rcvDraw = new fabric.Line(data.draw,data);
        this.canvas.add(rcvDraw);
        console.log("받아서 그린 것 : ",rcvDraw);
        this.canvas.renderAll();

        // console.log(`보낸 메세지: , x: ${x} y: ${y} prevX: ${prevX}, prevY: ${prevY}, color: ${color}, fillColor: ${fillColor}, lineWidth:${lineWidth}, rect: ${rect}, triangle: ${triangle}, circle: ${circle}` , data);

      }
    },
  },
};
</script>

<style scoped>

.fabric {
  background: #2c4332;
  /*width: 100%;
  height: 100%;*/
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
