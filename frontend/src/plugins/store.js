import { createStore } from 'vuex';
import { Client } from "@stomp/stompjs";

let stompClient = null;
let ck = null;
const createStompClient = (commit) => {
    return new Client({
        brokerURL: "ws://localhost:8080/whiteboard",
        debug: function (str) {
            console.log(str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
            console.log('Connected: ' + frame);
            commit('setSocket', stompClient);
        },
        onStompError: (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        }
    });
};

const store = createStore({
    state() {
        return {
            events: [],
            joins: [],
            leaves: [],
            socket: null,
        };
    },
    mutations: {
        addEvent(state, event) {
            state.events.push(event);
        },
        addJoin(state, join) {
            state.joins.push(join);
        },
        addLeave(state, leave) {
            state.leaves.push(leave);
        },
        setSocket(state, socket) {
            state.socket = socket;
        }
    },
    actions: {
        triggerEvent({ commit }, event) {
            commit('addEvent', event);
        },
        triggerJoin({ commit }, join) {
            commit('addJoin', join);
        },
        triggerLeave({ commit }, leave) {
            commit('addLeave', leave);
        },
        initializeWebSocket({ commit }, classCode) {
            return new Promise((resolve, reject) => {
                if (stompClient && stompClient.connected) {
                    stompClient.deactivate();
                }

                stompClient = createStompClient(commit);
                stompClient.onConnect = () => {
                    console.log('Connected');
                    commit('setSocket', stompClient);
                    resolve();
                };

                stompClient.onStompError = (frame) => {
                    reject(new Error('Broker reported error: ' + frame.headers['message']));
                };

                stompClient.activate();
            });
        },
        subscribeToClass({ dispatch }, classCode) {
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
            } else {
                console.error('StompClient is not connected.');
            }
        }
    },
});

export default store;
