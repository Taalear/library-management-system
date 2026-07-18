import { reactive } from 'vue'

export const appState = reactive({
    avatar: localStorage.getItem('avatar') || ''
})