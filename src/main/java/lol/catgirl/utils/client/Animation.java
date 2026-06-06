package lol.catgirl.utils.client;

import lombok.Getter;
import lombok.Setter;

public class Animation {
    @Getter
    private final Easing easing; 
    @Getter @Setter
    private long duration, millis, startTime; 
    @Getter @Setter

    private float startValue, destValue, value; 
    @Getter @Setter

    private boolean finished;
    
    public Animation(Easing easing, long duration) {
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    } 

    public void run(float destValue) {
        this.millis = System.currentTimeMillis();
        if(this.destValue != destValue) {
            this.destValue = destValue;
            this.reset();
        } else {
            this.finished = this.millis - this.duration > this.startTime || this.value == destValue;
            if(this.finished) {
                this.value = destValue;
                return;
            }
        } 

        float result = this.easing.getFunction().apply(this.getProgress()); 


        if (this.value > destValue) {
            this.value = this.startValue - (this.startValue - destValue) * result;
        } else {
            this.value = this.startValue + (destValue - this.startValue) * result;            
        } 

        if(Float.isNaN(value) || !Float.isFinite(value)) {
            this.value = destValue;
        }
    } 

    public float getProgress() {
        float progress = (float)(System.currentTimeMillis() - this.startTime) / (float)this.duration;
        return Math.min(progress, 1.0f);
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.startValue = value;
        this.finished = false;
    } 
}
