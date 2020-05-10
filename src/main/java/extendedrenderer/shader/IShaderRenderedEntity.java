package extendedrenderer.shader;


import net.minecraft.client.renderer.Vector3f;

public interface IShaderRenderedEntity {

    Vector3f getPosition();
    //Quaternion getQuaternion();
    //Quaternion getQuaternionPrev();
    //Vector3f getScale();
    float getScale();
    //boolean hasCustomMatrix();


}
