const apiBase = window.location.pathname.replace(/\/ui\/.*$/, "")

const {createApp} = Vue

createApp({
  data() {
    return {
      name: "",
      message: ""
    }
  },

  methods: {
    async callBackend() {
      const r = await fetch(
          `${apiBase}/goodbye?input=${encodeURIComponent(this.name)}`)
      this.message = await r.text()
    }
  },

  template: `
    <div class="plugin">
      <h2>Goodbye Plugin</h2>

      <input v-model="name" placeholder="Name">
      <button @click="callBackend">Send</button>

      <p class="result">{{ message }}</p>
    </div>
  `
}).mount("#app")