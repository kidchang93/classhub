import { createStore } from 'vuex';
import { Client } from "@stomp/stompjs";

let stompClient = null;
// STOMP 클라이언트를 생성하는 함수
const createStompClient = (commit) => { //commit 메서드를 인자로 받아 새로운 Client 객체를 반환
    return new Client({
        brokerURL: "ws://localhost:8080/whiteboard",
        debug: function (str) {
            console.log(str);
        },
        reconnectDelay: 5000, //재연결 시도 간격(밀리초)입니다
        heartbeatIncoming: 4000, //클라이언트와 서버 간의 heartbeat 간격(밀리초)입니다.
        heartbeatOutgoing: 4000,
        onConnect: (frame) => { //연결이 성공했을 때 호출되는 콜백 함수입니다
            console.log('Connected: ' + frame);
            commit('setSocket', stompClient);
        },
        onStompError: (frame) => { // STOMP 에러가 발생했을 때 호출되는 콜백 함수입니다.
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        }
    });
};

const store = createStore({
    state() { //애플리케이션의 상태를 정의
        return {
            events: [],
            joins: [],
            leaves: [],
            pickerStarts: [],
            pickerEnds: [],
            pickerSelects: [],
            socket: null,
        };
    },
    mutations: { //상태를 변경하는 메서드
        addEvent(state, event) {
            state.events.push(event);
        },
        addJoin(state, join) {
            state.joins.push(join);
        },
        addLeave(state, leave) {
            state.leaves.push(leave);
        },
        addPickerStart(state, pickerStart) {
            state.pickerStarts.push(pickerStart);
        },
        addPickerEnd(state, pickerEnd) {
            state.pickerEnds.push(pickerEnd);
        },
        addPickerSelect(state, pickerSelect) {
            state.pickerSelects.push(pickerSelect);
        },
        setSocket(state, socket) {
            state.socket = socket;
        }
    },
    actions: { //비동기 작업을 수행하며, mutations를 커밋합니다.
        triggerEvent({ commit }, event) {
            /* commit은 vuex에서 지원하는 메소드. 
            commit(type, payload): 
            type은 mutations의 이름 문자열,
            payload는 mutations에 전달할 추가데이터(객체, 배열, 숫자, 문자열 등 어떤 유형의 데이터든 전달) */
            commit('addEvent', event); 
        },
        triggerJoin({ commit }, join) {
            commit('addJoin', join);
        },
        triggerLeave({ commit }, leave) {
            commit('addLeave', leave);
        },
        triggerPickerStart({ commit }, pickerStart) {
            commit('addPickerStart', pickerStart);
        },
        triggerPickerEnd({ commit }, pickerStart) {
            commit('addPickerStart', pickerStart);
        },
        triggerPickerSelect({ commit }, pickerSelect) {
            commit('addPickerSelect', pickerSelect);
        },
        initializeWebSocket({ commit }) {
            return new Promise((resolve, reject) => {
                if (stompClient && stompClient.connected) {
                    stompClient.deactivate();
                }

                stompClient = createStompClient(commit);
                stompClient.onConnect = () => {
                    console.log('Connected');
                    /*비동기 작업 중 상태 변경이 필요할 때 commit을 통해 
                    setSocket mutation을 호출하여 상태를 변경합니다.*/
                    commit('setSocket', stompClient); 
                    resolve();
                };

                stompClient.onStompError = (frame) => {
                    reject(new Error('Broker reported error: ' + frame.headers['message']));
                };

                stompClient.activate();
            });
        },
        subscribeToClass({ dispatch }, { classCode, userType }) {
            if (stompClient && stompClient.connected) {
                stompClient.subscribe(`/sub/class/${classCode}`, (message) => {
                    console.log('Received message: ', message.body);
                    dispatch('triggerEvent', JSON.parse(message.body));
                });
                stompClient.subscribe(`/sub/class/${classCode}/join`, (message) => {
                    console.log('Received join message: ', message.body);
                    dispatch('triggerJoin', JSON.parse(message.body));
                });
                stompClient.subscribe(`/sub/class/${classCode}/leave`, (message) => {
                    console.log('Received leave message: ', message.body);
                    dispatch('triggerLeave', JSON.parse(message.body));
                });
                if (userType === 'student') {
                    stompClient.subscribe(`/sub/class/${classCode}/picker/start`, (message) => {
                        console.log('Received picker start message: ', message.body);
                        dispatch('triggerPickerStart', JSON.parse(message.body));
                    });
                    stompClient.subscribe(`/sub/class/${classCode}/picker/end`, (message) => {
                        console.log('Received picker end message: ', message.body);
                        dispatch('triggerPickerEnd', JSON.parse(message.body));
                    });
                } else if (userType === 'teacher') {
                    stompClient.subscribe(`/sub/class/${classCode}/picker/select`, (message) => {
                        console.log('Received picker select message: ', message.body);
                        dispatch('triggerPickerSelect', JSON.parse(message.body));
                    });
                }
            } else {
                console.error('StompClient is not connected.');
            }
        }
    },
});

export default store;
