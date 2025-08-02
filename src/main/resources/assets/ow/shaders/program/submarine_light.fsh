#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;

uniform mat4 ProjMat;
uniform vec2 OutSize;
uniform vec2 ScreenSize;
uniform float _FOV;

in vec2 texCoord;
out vec4 fragColor;

float near = 0.1;
float far = 10.0;
float exposure = 20;
float AOE = 15;

vec2 lightPos1 = vec2(0.425, 0.5);
vec2 lightPos2 = vec2(0.575, 0.5);

float LinearizeDepth(float depth)
{
    float z = depth * 2.0f - 1.0f;
    return (near * far) / (far + near - z * (far - near));
}

float calculateLightEffect(vec2 uv, vec2 lightCenter) {
    float d = sqrt(pow((uv.x - lightCenter.x), 2.0) + pow((uv.y - lightCenter.y), 2.0));
    return exp(-(d * AOE)) * exposure;
}

void main(){
    vec2 uv = texCoord;

    float lightEffect1 = calculateLightEffect(uv, lightPos1);
    float lightEffect2 = calculateLightEffect(uv, lightPos2);

    float totalLightEffect = lightEffect1 + lightEffect2;

    float rawDepth = texture(DiffuseDepthSampler, texCoord).r;
    float depthFactor = 1.0 - smoothstep(0.95, 1.0, rawDepth) * 0.8;
    totalLightEffect *= depthFactor;

    vec4 originalColor = texture(DiffuseSampler, texCoord);

    vec3 lightColor = vec3(1.2, 1.0, 0.8);
    fragColor = vec4(
        originalColor.rgb * clamp(1.0 + totalLightEffect, 0.0, 10.0) * lightColor,
        originalColor.a
    );
}