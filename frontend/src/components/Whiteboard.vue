<template>

  <canvas class="fabric" ref="canvas"></canvas>

    <div id="designTool">
      <button type="button" class="btn btn-secondary" @click="buttonErase">모두 지우기</button>
      <br>
      <button type="button" class="btn btn-secondary" @click="eraser">도형 지우개</button>
      <br>
      <button type="button" class="btn btn-secondary" @click="activateEraserMode('eraserBrush')">드로잉 지우개</button>
      <br>
      <button type="button" class="btn btn-secondary" @click="createGroup(objects)">그룹 Go!</button>
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
              @click="toggleDrawMode('brush')">그리기
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
import {parse, v4 as uuidv4} from "uuid";
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
      erasedObjects: [],
      objects:[],
      // 화면 속성 변수

      // 속성 변수
      lineWidth: 10,
      color: '#ffffff',
      // old 객체 변수
      oldRect:{},
      oldCircle:{},
      oldTriangle:{},
      selectObj:{},
      objectId: '',
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
      this.brush = this.canvas.freeDrawingBrush;
      this.brush.color = this.color;
      this.brush.width = parseInt(this.lineWidth);

      // 이벤트 리스너 추가

        this.canvas.on('mouse:down', (e) => {
          if (this.mode == 'brush' || this.mode == 'eraserBrush'){
            const pointer = this.canvas.getPointer(e);
            this.x = pointer.x
            this.y = pointer.y
            this.drawing = true;
            this.handleMouseDown(e)
          } else if (this.mode == 'click'){
            this.clickObject(e)
          }
        })
      this.canvas.on('mouse:move', (e) => {
        if (this.mode == 'brush' && this.drawing == true || this.mode == 'eraserBrush' && this.drawing == true) {
          // prevXY 정의
          this.prevX = this.x;
          this.prevY = this.y;
          this.handleMouseMove(e)
        }
      })
      this.canvas.on('mouse:up', (e) => {
        // this.drawing = false;
        if (this.mode == 'brush'){
          this.handleMouseUp(e)
        } else if (this.mode == 'eraserBrush'){
          this.handleMouseUp(e)
        }
      })

      // add 될 때 uuid 부여하면 학생쪽도 생기는 이벤트니까 각각 다른 값 들어감 add 메서드에서 하자
      this.canvas.on('object:added', (e) => {
        // uuid 값 부여함.
        this.objectId = uuidv4();
        e.target.id = this.objectId;
        console.log("아이디 부여 : ",e.target.id)
        const a = this.canvas.getObjects();
        console.log("a = ",a)
        this.objects = a;

      })

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


      // 지우기 끝난 후 호출되는 이벤트 리스너

      console.log("initCanvas");

    },

    // 지우개 커스텀 클래스 함수 정의
    EraserBrush: fabric.util.createClass(fabric.PencilBrush, {
        _finalizeAndAddPath: function() {
          const ctx = this.canvas.contextTop;
          ctx.closePath();
          if (this.decimate) {
            this._points = this.decimatePoints(this._points, this.decimate);
          }
          const pathData = this.convertPointsToSVGPath(this._points).join('');
          if (pathData === 'M 0 0 Q 0 0 0 0 L 0 0') {
            this.canvas.requestRenderAll();
            return;
          }

          const path = this.createPath(pathData);
          path.globalCompositeOperation = 'destination-out';  // 이 설정이 중요 지워지게하는 핵심임
          this.canvas.clearContext(this.canvas.contextTop);
          this.canvas.add(path);
          this.canvas.requestRenderAll();
          this._resetShadow();
        },
    }),

    // 지우개 모드 가동
    activateEraserMode(mode) {
      this.mode = mode;
      this.canvas.isDrawingMode = true;
      this.canvas.freeDrawingBrush = new this.EraserBrush(this.canvas);
      this.canvas.freeDrawingBrush.width = parseInt(this.lineWidth);
      this.canvas.freeDrawingBrush.color = this.canvas.backgroundColor;
      // this.canvas.freeDrawingBrush.selectable = false;

    },

    handleMouseDown(e) {
      const pointer = this.canvas.getPointer(e);

      this.canvas.isDrawingMode = true;
      this.brush.color = this.color;
      this.brush.width = parseInt(this.lineWidth);

      if (this.mode == 'brush'){

        let drawData = {
          x: pointer.x,
          y: pointer.y,
          color: this.brush.color,
          width: this.brush.width,
        };

        this.sendMessage('DRAW',drawData);
        console.log("그리기 다운 : ",drawData)
      } else if (this.mode == 'eraserBrush') {

        // this.brush.width = parseInt(this.lineWidth);

        let drawData = {
          x: pointer.x,
          y: pointer.y,
          color: this.canvas.backgroundColor,
          width: this.brush.width,
        };

        this.sendMessage('eraserBrush',drawData);
        console.log("지우개 다운 : ",drawData)
      }
    },
    handleMouseMove(e) {
      this.canvas.isDrawingMode = true;
      this.brush.color = this.color;
      this.brush.width = parseInt(this.lineWidth);
      const pointer = this.canvas.getPointer(e);
      // 현재 좌표
      this.x = pointer.x;
      this.y = pointer.y;

      if (this.mode == 'brush'){

        let drawData = {
          x: this.x,
          y: this.y,
          prevX: this.prevX,
          prevY: this.prevY,
          color: this.color,
          width: this.brush.width,
        };
        console.log("브러쉬 무브",drawData);
        this.sendMessage('DRAW',drawData)
      } else if (this.mode == 'eraserBrush'){

        let drawData = {
          x: this.x,
          y: this.y,
          prevX: this.prevX,
          prevY: this.prevY,
          color: this.canvas.backgroundColor,
          width: this.brush.width,
        };
        // console.log("지우개 무브",drawData);
        // this.sendMessage('eraserBrush',drawData)

        const a = this.canvas.containsPoint(pointer);
        console.log("aaaaaaaaaa : ", a)
      }


    },
    handleMouseUp(e) {
      this.drawing = false;
      this.canvas.isDrawingMode = true;
      this.brush.color = this.color;
      this.brush.width = parseInt(this.lineWidth);
      if (this.mode == 'brush'){

        console.log("마우스 업", this.brush);

      } else if (this.mode == 'eraserBrush'){

        // const erasedObjectsAfterGet = this.getErasedObjects();   // 추출한 함수를 담는 배열
        //
        //
        // if (erasedObjectsAfterGet.length > 0){
        //   console.log("여기 도냐")
        //   const group = new fabric.Group(erasedObjectsAfterGet);
        //   console.log("그룹 정보 : ", group);
        //   this.canvas.add(group);
        //   this.canvas.renderAll();
        // }




        console.log("마우스 업", this.brush);


      }
    },
    createGroup(e){
      console.log("eeeeeee",e)
      const erasedObjectsAfterGet = e;   // 추출한 함수를 담는 배열


      if (erasedObjectsAfterGet.length > 0){
        console.log("여기 도냐")
        const group = new fabric.Group(erasedObjectsAfterGet);
        console.log("그룹 정보 : ", group);
        this.canvas.add(group);
        this.canvas.renderAll();
      }
    },
    // 지워진 객체들을 추출하는 함수
    getErasedObjects(){
      // 기존 배열 초기화
      const erasedObjects = [];
      const objects = this.canvas.getObjects();

      for (let obj of objects){
        if (obj.globalCompositeOperation === 'destination-out'){
          console.log("지워진 객체 : ",obj)
          const cloneObj = fabric.util.object.clone(obj);

          erasedObjects.push(cloneObj);
        }
      }
      this.erasedObjects = erasedObjects;
      console.log("erasedObjects : ",erasedObjects);
      return erasedObjects;
      // console.log("지워진 객체 정보 : ", this.erasedObjects)
    },

    test(){
      // rect에 집어넣은 변수값으로 생성된다. 이게 되네...
      const newRect = new fabric.Rect(this.rect);
      this.canvas.add(newRect);
      console.log(newRect)
    },


    eraser(e){
      this.drawing = false;
      this.canvas.isDrawingMode = false;
      this.mode = 'eraser';

      const pointer = this.canvas.getPointer(e);
      const selectObj = this.canvas.getObjects().find(obj => obj.containsPoint(pointer));
      console.log("selectObj : ", selectObj);
      if (selectObj){
        this.canvas.remove(selectObj);
        this.sendMessage('eraser',selectObj);

      }

    },
    // 선 굵기 변경
    changeLineWidth(e) {
      this.lineWidth = e.target.value;
      this.brush.width = parseInt(this.lineWidth);
      console.log("선 굵기 : ",this.brush.width);

    },
    // 선 색상 변경
    changeColor(e){
      this.color = e.target.value;
      this.brush.color = this.color;
      console.log('색깔 : ', this.brush.color);
    },
    changeFillColor(e){
      this.fillColor = e.target.value;
    },


    // 기존 이벤트 핸들러 제거 => initial 함수에서 조건에 맞는 함수로 event 보내기

    sendMessage(type, event){
      console.log("sendType : ", type)
      console.log("sendMessage : ",event)
      // Drawing 메시지 전송
      if (type == 'DRAW') {
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          clientId: this.clientId,
          data: event,
        });
        console.log("지금 모드는 : ", this.mode)
      } else if (type == 'rect') {
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          clientId: this.clientId,
          id: this.objectId,
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
      } else if (type == 'eraser'){
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          clientId: this.clientId,
          id: this.objectId,
          data: event,
        });
      } else if (type == 'eraserBrush'){
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
        erasable:true,
        id:uuidv4(),
        }
      )
      this.rect = rect;
      this.oldRect = rect;
      this.objectId = rect.id;
      this.canvas.add(rect);
      // this.canvas.setActiveObject(rect);
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
    toggleDrawMode(mode) {
      this.mode = mode;
      this.canvas.isDrawingMode = true;
      this.canvas.freeDrawingBrush = new fabric.PencilBrush(this.canvas);
      this.brush = this.canvas.freeDrawingBrush;
      this.brush.color = this.color;
      this.brush.width = parseInt(this.lineWidth);
      console.log("그리기 버튼 : ", this.brush)
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
    clickObject(e){
      this.mode = 'click';
      this.canvas.isDrawingMode = false;
      this.drawing = false;
      this.objectId = e.target;
      console.log("지금 객체는 : ",  this.objectId);
    },


    // 메시지 수신 처리 함수
    handleIncomingDrawing(message) {
      const {type, data} = message;
      console.log("받은 message : ", message);
      const clientId = message.clientId;
      if (clientId == this.clientId){
        return;
      }
      if (type == 'DRAW' || type == 'eraserBrush') {

        const { x, y, prevX, prevY, color, width } = data;
        console.log("학생 data : ",data);
        const context = this.canvas.getContext('2d');

        context.strokeStyle = color;
        context.lineWidth = width;
        context.lineCap = this.lineCap;
        context.beginPath();
        context.moveTo(prevX, prevY);
        context.lineTo(x, y);
        context.stroke();

      }
      if (type == 'rect'){

        const newRect = new fabric.Rect(data);

        this.canvas.add(newRect);

        this.canvas.renderAll();
      }
      if (type == 'circle'){
        const newCircle = new fabric.Circle(data);
        // this.canvas.clear(this.oldCircle);
        this.canvas.add(newCircle);
      }
      if (type == 'triangle'){
        const triangle = new fabric.Triangle(data);
        // this.canvas.clear(this.oldTriangle);
        this.canvas.add(triangle);
      }
      if (type == 'erase'){
        this.canvas.clear();
      }
      if (type == 'eraser'){
        console.log("data.id : ",data.id);
        const objectToRemove = this.canvas.getObjects().find(obj => obj.id === data.id);

        console.log("objectToRemove : ", objectToRemove);
        if (objectToRemove){

          this.canvas.remove(objectToRemove);
          this.canvas.renderAll();

        }



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
