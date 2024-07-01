<template>

  <canvas class="fabric" ref="canvas"></canvas>

    <div id="designTool">
      <button type="button" class="btn btn-secondary" @click="buttonErase">새로 시작</button>
      <br>
      <button type="button" class="btn btn-secondary" @click="toggleMode('delete')">삭제</button>
      <br>
      <button type="button" class="btn btn-secondary" @click="activateEraserMode('eraserBrush')">드로잉 지우개</button>
      <br>
<!--      <button type="button" class="btn btn-secondary" @click="createGroup(objects)">그룹 Go!</button>-->
<!--      <br>-->
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
              @click="toggleMode('click')">객체 선택
      </button>
    </div>

</template>

<script>
import { mapState } from "vuex";
import {fabric} from "fabric";
import { v4 as uuidv4} from "uuid";
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
      eraserBrush:{},
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
      objects:{},
      targetObject: [],
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
      this.canvas.freeDrawingBrush = new fabric.PencilBrush(this.canvas);
      this.brush = this.canvas.freeDrawingBrush;
      this.brush.color = this.color;
      this.brush.width = parseInt(this.lineWidth);
      this.brush.evented = false;

      // 지우개 브러쉬 초기 설정
      this.canvas.freeDrawingBrush = new this.EraserBrush(this.canvas);
      this.eraserBrush = this.canvas.freeDrawingBrush;
      this.eraserBrush.color = this.canvas.backgroundColor;
      this.eraserBrush.width = parseInt(this.lineWidth);

      // 이벤트 리스너 추가
      // 그리기, 드로잉 관련 마우스 업 시 select 하지 못하게 함.
      this.canvas.on('path:created', (e)=> {
        var path = e.path;
        path.selectable = false;
        this.canvas.add(path);

      })
      this.canvas.on('mouse:down', (e) => {
        if (this.mode == 'brush' || this.mode == 'eraserBrush'){
          const pointer = this.canvas.getPointer(e);
          this.x = pointer.x
          this.y = pointer.y
          this.drawing = true;
          this.handleMouseDown(e)
        } else if (this.mode == 'click'){
          this.clickObject(e)
        } else if (this.mode == 'delete'){
          this.deleteObject(e)
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
        this.drawing = false;
        if (this.mode == 'brush'){
          this.handleMouseUp(e)
        } else if (this.mode == 'eraserBrush'){
          this.handleMouseUp(e)
        }
      })

      // 이 행위 자체로는 선생님 쪽에서 움직이는 객체에 대해 전송한다.
      this.canvas.on('object:modified', (e) => {
        if (this.mode == 'rect'){
          let modRect = e.target;
          console.log("움직였어 데이터 : ",modRect);
          console.log(this.rect);
          this.sendMessage('rect',modRect);
        } else if (this.mode == 'circle'){
          let modCircle = e.target;
          this.sendMessage('circle', modCircle);
        } else if (this.mode == 'triangle'){
          let modTriangle = e.target;
          this.sendMessage('triangle', modTriangle);
        }
      })

      this.canvas.renderAll();
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
            this.canvas.renderAll();
            // return;
          }

          const path = this.createPath(pathData);
          path.globalCompositeOperation = 'destination-out';  // 이 설정이 중요 지워지게하는 핵심임
          path.selectable = false;
          this.canvas.clearContext(ctx);
          this.canvas.add(path);
          this.canvas.renderAll();
          this._resetShadow();
          console.log("커스텀 지우개 생성")
        },
    }),

    // 지우개 모드 가동
    activateEraserMode(mode) {
      this.mode = mode;
      this.canvas.isDrawingMode = true;

      // 지우개 브러쉬 초기 설정
      this.canvas.freeDrawingBrush = new this.EraserBrush(this.canvas);
      this.eraserBrush = this.canvas.freeDrawingBrush;
      this.eraserBrush.color = this.canvas.backgroundColor;
      this.eraserBrush.width = parseInt(this.lineWidth);
      this.canvas.renderAll();
    },

    handleMouseDown(e) {
      const pointer = this.canvas.getPointer(e);

      this.canvas.isDrawingMode = true;
      this.brush.color = this.color;
      this.brush.width = parseInt(this.lineWidth);

      this.eraserBrush.color = this.canvas.backgroundColor;
      this.eraserBrush.width = parseInt(this.lineWidth);

      if (this.mode == 'brush'){

        let drawData = {
          x: pointer.x,
          y: pointer.y,
          color: this.brush.color,
          width: this.brush.width,
        };

        this.sendMessage('DRAW',drawData);

      } else if (this.mode == 'eraserBrush') {

        // this.brush.width = parseInt(this.lineWidth);

        let drawData = {
          x: pointer.x,
          y: pointer.y,
          color: this.eraserBrush.color,
          width: this.eraserBrush.width ,
        };

        this.sendMessage('eraserBrush',drawData);

      }
    },
    handleMouseMove(e) {
      this.canvas.isDrawingMode = true;
      this.brush.color = this.color;
      this.brush.width = parseInt(this.lineWidth);

      this.eraserBrush.color = this.canvas.backgroundColor;
      this.eraserBrush.width = parseInt(this.lineWidth);

      const pointer = this.canvas.getPointer(e);

      // 현재 좌표
      this.x = pointer.x;
      this.y = pointer.y;
      // 경로에 객체가 있을 때 마우스 위치에 있는 객체 정보 불러옴
      // const isOverObject = this.canvas.getObjects().some(obj => obj.containsPoint(pointer));
      // console.log("객체 있음? : ",isOverObject);

      if (this.mode == 'brush'){

        let drawData = {
          x: this.x,
          y: this.y,
          prevX: this.prevX,
          prevY: this.prevY,
          color: this.brush.color,
          width: this.brush.width,
        };

        this.sendMessage('DRAW',drawData)
      } else if (this.mode == 'eraserBrush'){

        let drawData = {
          x: this.x,
          y: this.y,
          prevX: this.prevX,
          prevY: this.prevY,
          color: this.eraserBrush.color,
          width: this.brush.width,
        };
        this.sendMessage('eraserBrush',drawData)
      }
    },
    handleMouseUp(e) {
      this.drawing = false;
      this.canvas.isDrawingMode = true;

      if (this.mode == 'brush'){

        this.brush.color = this.color;
        this.brush.width = parseInt(this.lineWidth);
        console.log("마우스 업", this.brush);

      } else if (this.mode == 'eraserBrush'){

        this.eraserBrush.color = this.canvas.backgroundColor;
        this.eraserBrush.width = parseInt(this.lineWidth);
        // this.getObject(e);  // 여기서 targetObject에 해당 객체 정보 넣음
        console.log("마우스 업", this.eraserBrush);

      }
    },
    // createGroup(obj){
    //   // 여기서 뭔가 가공을 해야되나...
    //   const upObj = obj;
    //
    //   console.log("createGroup - obj : ",upObj)
    //   if (upObj.length > 0){
    //
    //     const group = new fabric.Group(upObj,{
    //       erasable:'deep',
    //     });
    //     // 이거 하나 넣었다고 그룹은 안되는데
    //     // 아까 현상은 없어짐... 뭐지 진짜
    //     // group._restoreObjectsState();
    //
    //     console.log("createGroup - group : ",group)
    //     this.canvas.add(group);
    //     this.canvas.renderAll();
    //   }
    //
    // },
    // getObject(e){
    //   console.log("getObject - e.target: ",e.target)
    //
    //   const pointer = this.canvas.getPointer(e);
    //   // 경로에 객체가 있을 시
    //   this.canvas.getObjects().forEach((obj) => {
    //     if (obj.containsPoint(pointer)){
    //
    //       this.targetObject.push(obj);
    //       console.log("targetObject : ",this.targetObject)
    //       // 마우스 위치에 있는 객체 정보 불러옴
    //     }
    //   })
    //   console.log("getObject - this.targetObject: ",this.targetObject)
    //   // 하나는 proxy로 감싸져있고 하나는 그냥 klass임 그래서 뭔가 이상해짐
    //   const eraseObj = [e.target,this.targetObject];  // 배열 초기화 시키고
    //   // eraseObj.push(e.target,this.targetObject)// 배열에 그룹 만들 애들 넣어줌.
    //   console.log("push : ", eraseObj);
    //   // this.objects 하면 지금 캔버스에 추가된 모든 object를 불러옴 -> 이거는 쓰면 안됨.
    //   // eraseObj 로 넣으나 this.targetObject로 넣으나 결과는 똑같음
    //   // 근데 this.targetObject로 넣으면 애초에 proxy로 들어가서 그나마 console에서 보기는 좋음.
    //   this.createGroup(this.targetObject);   // 그리고 그룹 만들어
    //   // 접근은 좋은데 드래그로 선택하면 이상해짐
    //   // mouseUp 할때 뭔가 걸리는거거나 group 쪽이 안맞는거일수도?
    // },
    // test(){
    //   // rect에 집어넣은 변수값으로 생성된다. 이게 되네...
    //   const newRect = new fabric.Rect(this.rect);
    //   this.canvas.add(newRect);
    //   console.log(newRect)
    // },


    deleteObject(e){

      const pointer = this.canvas.getPointer(e);
      // 포인터로 찾으면 안되겠다.
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
      this.eraserBrush.width = parseInt(this.lineWidth);
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
          id: this.objectId,
          data: event,
        });
      } else if (type == 'triangle') {
        this.message = JSON.stringify({
          type: type,
          sender: this.sender,
          clientId: this.clientId,
          id: this.objectId,
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
        dirty: true,
        selectable: true,
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
        id:uuidv4(),
      })
      this.circle = circle;
      this.oldCircle = circle;
      this.canvas.add(circle);

      this.canvas.renderAll();
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
        id:uuidv4(),
      })
      this.triangle = triangle;
      this.oldTriangle = triangle;
      this.canvas.add(triangle);
      this.canvas.renderAll();
      this.sendMessage("triangle",triangle);
      console.log("triangle", triangle);
      console.log("도형 추가")
    },
    // 객체 선택
    // selectObject(e){
    //   this.canvas.on('mouse:down',(e) => {
    //     const targetObject = e.target;
    //     console.log('타겟 : ', targetObject);
    //   })
    //   const selectObject = this.canvas.getActiveObjects();
    //   selectObject.forEach(obj => {
    //
    //   })
    //   console.log("selectObject : ",selectObject);
    //   this.canvas.renderAll();
    //   this.sendMessage(selectObject);
    // },
    // 전체삭제 버튼
    buttonErase() {
      alert("지울거야?")
      console.log("지금? : ", this.canvas.backgroundColor)
      let removeObjects = this.canvas.getObjects();
      removeObjects.forEach((obj) => {
        this.canvas.remove(obj);
      })
      console.log("배열 : ",removeObjects)

      // this.canvas.clear();
      this.canvas.renderAll();
      this.sendMessage('erase');

      console.log("지금? : ", this.canvas.backgroundColor)
      console.log('전체 삭제 후 : ', this.mode)
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

      } else if (mode == 'delete') {
        console.log("삭제: ",mode)
        this.canvas.isDrawingMode = false;
        this.drawing = false;
        this.mode = 'delete';
      } else if (mode == 'click'){
        this.canvas.isDrawingMode = false;
        this.drawing = false;
        this.mode = 'click';
      }
    },
    clickObject(e){
      this.mode = 'click';
      this.canvas.isDrawingMode = false;
      this.drawing = false;
      const targetData = e.target;
      if (targetData != null){
        this.objectId = e.target.id;
      } else {
        return
      }

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
      if (type == 'rect' || type == 'modRect'){
        const rect = new fabric.Rect(data);

        // let removeObjects = this.canvas.getObjects();
        // console.log("removeObject : ",removeObjects)
        // removeObjects.forEach((obj) => {
        //   if (obj.id == data.id){
        //     console.log("obj : ", obj);
        //     // this.canvas.remove(obj);
        //   }
        //
        // })

        this.canvas.add(rect);
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
        const objectToRemove = this.canvas.getObjects().find(obj => {
          if (obj.id == data.id){
            this.canvas.remove(obj);
            this.canvas.renderAll();
          }
        });
        console.log('objectToRemove ',objectToRemove);

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
