package com.abinbev.dsa.bus.event;

public class AttachmentEvent {

    private AttachmentEvent(){ }

    public static class AttachmentSavedEvent {

        private final boolean isSuccess;

        public AttachmentSavedEvent(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }

    public static class AttachmentUploadEvent {

        private final boolean isSuccess;

        public AttachmentUploadEvent(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }

}
