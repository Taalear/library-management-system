<template>
  <router-view />
  <!-- WebSocket 通知 -->
  <div v-if="notification" style="position: fixed; top: 80px; right: 20px; z-index: 9999;">
    <el-alert
      :title="notification"
      type="success"
      :closable="true"
      show-icon
      @close="notification = null"
      style="min-width: 300px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Stomp } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const notification = ref(null)
let stompClient = null

onMounted(() => {
  // 使用相对路径，由 Vite 代理转发到后端
  const socket = new SockJS('/ws')
  stompClient = Stomp.over(socket)
  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/notifications', (message) => {
      notification.value = message.body
      setTimeout(() => { notification.value = null }, 5000)
    })
  }, (error) => {
    console.warn('WebSocket 连接失败，但不影响核心功能', error)
  })
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.disconnect()
  }
})
</script>