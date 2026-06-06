#version 330

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

layout(std140) uniform MotionBlurConfig {
    float BlendFactor;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 currentColor = texture(InSampler, texCoord);
    vec4 previousColor = texture(PrevSampler, texCoord);
    fragColor = mix(currentColor, previousColor, BlendFactor);
    fragColor.a = 1.0;
}
