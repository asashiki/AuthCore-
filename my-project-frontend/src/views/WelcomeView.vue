<script setup>

</script>

<template>
  <div style="width:100vw;height:100vh;overflow: hidden;display:flex">
    <div style="flex:1; background-color: black">
      <el-image style="width: 100%; height: 100%" fit="cover"
                src="https://picture-img.leqazwsxedc.workers.dev/1.jpg"/>
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px; font-weight: bold">欢迎来到测试网站</div>
      <div style="margin-top: 10px">Java+Vue前后端搭建</div>
      <div style="margin-top: 5px">非要去找出他人的缺点…来确认自己比较优秀不这样做就无法安心吗？在对谁说话啊？</div>
    </div>
    <div class="right-card">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component"/>
        </transition>
      </router-view>
    </div>

  </div>
</template>

<style scoped>
.welcome-title {
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
.right-card {
  width: 400px;
  z-index: 1;
  background-color: white;
}
</style>