#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;

vec2 lightPos1 = vec2(0.40, 0.510);
vec2 lightPos2 = vec2(0.60, 0.510);

void main() {
    vec4  originalColor = texture(DiffuseSampler, texCoord);
    float rawDepth      = texture(DiffuseDepthSampler, texCoord).r;

    float skyMask = 1.0 - step(0.9999, rawDepth);

    float aspect = OutSize.x / OutSize.y;
    vec2 d1 = vec2((texCoord.x - lightPos1.x) * aspect, texCoord.y - lightPos1.y);
    vec2 d2 = vec2((texCoord.x - lightPos2.x) * aspect, texCoord.y - lightPos2.y);

    float spot1 = exp(-dot(d1, d1) * 22.0);
    float spot2 = exp(-dot(d2, d2) * 22.0);

    // High pow: very bright centre, fast drop-off → strong contrast
    float totalSpot = pow(clamp(spot1 + spot2, 0.0, 1.0), 4.5) * skyMask;

    // Blue-green with a slight warm hint
    vec3 lightColor = vec3(0.78, 0.92, 1.0);

    // More multiplicative weight = stronger block-to-block contrast
    vec3 additive       = lightColor * totalSpot * 0.30;
    vec3 multiplicative = originalColor.rgb * lightColor * totalSpot * 0.90;

    fragColor = vec4(originalColor.rgb + additive + multiplicative, originalColor.a);
}
