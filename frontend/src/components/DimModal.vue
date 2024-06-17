<template>
  <!-- Modal -->
  <div class="modal fade" id="exampleModalCenter" tabindex="-1" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title" id="exampleModalCenterTitle">{{ modalData.modalTitle }}</h3>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body text-center">
          <h4>{{ modalData.modalTitle }}로 입장하세요</h4>  
          <h3>{{ modalData.modalBody }}</h3>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
    export default {
        name: 'DimModal',
        props:{
            modalData: Object,
        }
    }
</script>

<style lang="scss" scoped>

</style>