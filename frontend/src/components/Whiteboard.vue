<template>

  <canvas class="fabric" ref="canvas"></canvas>

    <div id="designTool">
      <button type="button" class="btn btn-secondary" @click="buttonErase">모두 지우기</button>
      <br>
      <button type="button" class="btn btn-secondary" @click="eraser">지우개</button>
      <br>
      <label for="drawing-line-width">Line width : </label>
      <span class="info">{{ lineWidth }}</span>
      <input type="range" :value="lineWidth" min="0" max="100" id="drawing-line-width" ref="drawingLineWidthEl" @input="changeLineWidth"><br>

      <label for="drawing-color">그리기 색 : </label>
      <span class="info">{{ color }}</span>
      <input type="color" :value="color" id="drawing-color" ref="drawingColorEl" @input="changeColor"><br>

      <label for="drawing-color">도형 색 : </label>
      <span class="info">{{ fillColor }}</span>
      <input type="color" :value="fillColor" id="fill-color" ref="drawingFillColorEl" @input="changeFillColor"><br>

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
    clientId: String,
  },
  data() {
    return {

      // fabric
      mode:'',
      brush:{},
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
      // old 객체 변수
      oldRect:{},
      oldCircle:{},
      oldTriangle:{},

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
      this.$refs.canvas.width = 1920;
      this.$refs.canvas.height = 1080;
      // 브러시 초기 설정
      this.canvas.freeDrawingBrush.color = this.color;
      this.canvas.freeDrawingBrush.width = this.lineWidth;
      console.log("클라이언트 ID : ",this.clientId);
      console.log("초기 설정 : ",this.canvas);
      // 이벤트 리스너 추가

        this.canvas.on('mouse:down', (e) => {
          if (this.mode == 'brush'){
            const pointer = this.canvas.getPointer(e);
            this.x = pointer.x
            this.y = pointer.y
            this.drawing = true;
            this.handleMouseDown(e)
          }
        })
      this.canvas.on('mouse:move', (e) => {
        if (this.mode == 'brush' && this.drawing == true){
          // prevXY 정의
          this.prevX = this.x;
          this.prevY = this.y;
          this.handleMouseMove(e)
        }

      })
      this.canvas.on('mouse:up', (e) => {
        this.handleMouseUp(e)
        this.canvas.off('mouse:off')
      })

      // 여기는 이벤트에 반응을 해버리기 때문에 함부로 쓰면 안된다. 재귀처럼 동작함.
      // this.canvas.on('object:added', (e) => {
      //   this.sendMessage("rect",this.rect);
      // })

      // 이 행위 자체로는 선생님 쪽에서 움직이는 객체에 대해 전송한다.
      // 지금은 웹소켓의 설정으로 인해 다시 학생에게 추가된 객체에 대해서는 받고있는 상황.
      // 학생쪽에서 해당 객체를 컨트롤하면 보내진 않음.
      this.canvas.on('object:modified', (e) => {
        if (this.mode == 'rect'){
          let modRect = this.rect;
          this.sendMessage('rect',modRect);
        } else if (this.mode == 'circle'){
          let modCircle = this.circle;
          this.sendMessage('circle', modCircle);
        } else if (this.mode == 'triangle'){
          let modTriangle = this.triangle;
          this.sendMessage('triangle', modTriangle);
        }
      })

      console.log("initCanvas");

    },
    handleMouseDown(e) {
      const pointer = this.canvas.getPointer(e);

      this.brush = this.canvas.freeDrawingBrush;

      let drawData = {
        x: pointer.x,
        y: pointer.y,
        color: this.color,
        width: this.lineWidth,
      };

      if (this.drawing == true) {
        this.canvas.isDrawingMode = true;

        this.canvas.freeDrawingBrush.color = this.color;
        this.canvas.freeDrawingBrush.trans = this.color;
        this.canvas.freeDrawingBrush.width = parseInt(this.lineWidth);

        console.log("마우스 다운")
      }
      this.sendMessage('DRAW',drawData)

    },
    handleMouseMove(e) {

      const pointer = this.canvas.getPointer(e);
      // 현재 좌표
      this.x = pointer.x;
      this.y = pointer.y;

      let drawData = {
        x: this.x,
        y: this.y,
        prevX: this.prevX,
        prevY: this.prevY,
        color: this.color,
        width: this.lineWidth,
      };

      console.log("[this.prevXY] : ",this.prevX, this.prevY);
      console.log("[this.xY] : ",this.x, this.y);
      console.log("마우스 무브")
      this.sendMessage('DRAW',drawData)
    },
    handleMouseUp(e) {

      this.drawing = false;

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
      this.mode = 'eraser';
      const eraserBrush = fabric.util.createClass(new fabric.PencilBrush)
      eraserBrush.prototype.erasable = true;
      console.log("지우개 모드 맞음? : ",eraserBrush);
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
          clientId: this.clientId,
          data: event,
        });
      } else if (type == 'rect') {
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          clientId: this.clientId,
          data: event,
        });
      } else if (type == 'circle') {
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          clientId: this.clientId,
          data: event,
        });
      } else if (type == 'triangle') {
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          clientId: this.clientId,
          data: event,
        });
      } else if (type == 'erase') {
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          clientId: this.clientId,
          data: event,
        });
      }
      if (this.socket && this.socket.connected) {
        this.socket.publish({
          destination: `/pub/update/${this.classCode}`,
          body: this.message,
        });
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
      this.oldRect = rect;
      this.canvas.add(rect);
      this.canvas.setActiveObject(rect);
      this.canvas.renderAll();

      this.sendMessage("rect",rect);
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
      this.oldCircle = circle;
      this.canvas.add(circle);
      this.canvas.setActiveObject(circle);
      // this.canvas.renderAll();
      this.sendMessage("circle",circle);
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
      this.oldTriangle = triangle;
      this.canvas.add(triangle);
      this.canvas.setActiveObject(triangle);
      // this.canvas.renderAll();
      this.sendMessage("triangle",triangle);
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
      this.sendMessage('erase');
    },
    // 그리기 버튼
    toggleDrawMode() {
      this.mode = 'brush';

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
        this.mode = 'triangle'
        console.log(this.mode)
        this.addTriangle()
      }
    },
    clickObject(){
      this.mode = '';
      this.canvas.isDrawingMode = false;
      this.drawing = false;
    },

    // 메시지 수신 처리 함수
    handleIncomingDrawing(message) {
      const {type, data} = message;
      console.log("받은 message : ", message);
      const clientId = message.clientId;
      console.log("handle 밖 clientId : " ,clientId)

      if (clientId == this.clientId){
        return;
      }
      if (type == 'DRAW') {
        const { x, y, prevX, prevY, color, width } = data;

        const context = this.canvas.getContext('2d');
        context.strokeStyle = color;
        context.lineWidth = width;
        context.lineCap = this.lineCap;
        context.beginPath();
        context.moveTo(prevX, prevY);
        context.lineTo(x, y);
        context.stroke();

      }
      // if (type == 'DRAW'){
      //   const { x, y, prevX, prevY, color, width } = data;
      //   const brush = this.canvas.freeDrawingBrush;
      //   console.log("brush : " ,brush)
      //   brush.color = color;
      //   brush.width = width;
      //   brush._render(brush);
      //   // console.log("brush : " ,brush)
      // }
      if (type == 'rect'){
        const newRect = new fabric.Rect(data);
        this.canvas.clear(this.oldRect);
        this.canvas.add(newRect);
      }
      if (type == 'circle'){
        const newCircle = new fabric.Circle(data);
        this.canvas.clear(this.oldCircle);
        this.canvas.add(newCircle);
      }
      if (type == 'triangle'){
        const triangle = new fabric.Triangle(data);
        this.canvas.clear(this.oldTriangle);
        this.canvas.add(triangle);
      }
      if (type == 'erase'){
        this.canvas.clear();
      }
    }
  },
};
</script>

<style scoped>
.whiteboard {
  width: 100%;
  height: 100%;
}
.fabric {
  background: #2c4332;
  width: 100%;
  height: 100%;
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
