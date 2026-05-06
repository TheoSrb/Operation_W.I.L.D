#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform vec2 OutSize;

uniform float LightSeparation;
uniform float SingleBeam;
uniform float LightY;
uniform float SpotRadius;
uniform float ContrastPow;
uniform float AdditiveStrength;
uniform float MultiplicativeStrength;
uniform float ShadowFactor;
uniform vec3 LightColor;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4  originalColor = texture(DiffuseSampler, texCoord);
    float rawDepth      = texture(DiffuseDepthSampler, texCoord).r;

    float skyMask = 1.0 - step(0.9999, rawDepth);

    float aspect = OutSize.x / OutSize.y;
    // SingleBeam=1.0 → les deux positions fusionnent au centre
    vec2 lightPos1 = vec2(mix(0.5 - LightSeparation, 0.5, SingleBeam), LightY);
    vec2 lightPos2 = vec2(mix(0.5 + LightSeparation, 0.5, SingleBeam), LightY);

    vec2 d1 = vec2((texCoord.x - lightPos1.x) * aspect, texCoord.y - lightPos1.y);
    vec2 d2 = vec2((texCoord.x - lightPos2.x) * aspect, texCoord.y - lightPos2.y);

    float spot1 = exp(-dot(d1, d1) * SpotRadius);
    float spot2 = exp(-dot(d2, d2) * SpotRadius);

    float totalSpot = pow(clamp(spot1 + spot2, 0.0, 1.0), ContrastPow) * skyMask;

    float shadowEffect  = 1.0 - totalSpot * ShadowFactor;
    vec3 additive       = LightColor * totalSpot * AdditiveStrength;
    vec3 multiplicative = originalColor.rgb * LightColor * totalSpot * MultiplicativeStrength;

    fragColor = vec4(originalColor.rgb * shadowEffect + additive + multiplicative, originalColor.a);
}
