package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

public class Player {

    private Geometry geo;
    
    // AJUSTE: Subimos la altura inicial para que con el nuevo tamaño calce justo sobre la tierra
    private float posicionY = -2.7f; 

    public Player(AssetManager assetManager, Node rootNode) {
        // --- CORRECCIÓN CRÍTICA: TAMAÑO ---
        // Subimos el tamaño del plano a 4.5f de ancho y alto para que tenga una escala real 
        // y proporcional frente al tamaño gigante (20x10) que tienen tus fondos.
        Quad quad = new Quad(4.5f, 4.5f);
        geo = new Geometry("Player", quad);

        // Cargamos la textura del sprite
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/player/run.png");
        mat.setTexture("ColorMap", tex);

        // Activamos transparencias para limpiar imperfecciones
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.setFloat("AlphaDiscardThreshold", 0.1f);
        
        // Nos aseguramos de que sea visible por ambas caras
        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        geo.setMaterial(mat);
        geo.setQueueBucket(RenderQueue.Bucket.Transparent);

        // --- CORRECCIÓN DE POSICIÓN ---
        // Lo movemos a X = -5f (un poco más a la derecha para que no se salga por el borde izquierdo)
        // Lo dejamos en Z = -3f para que flote físicamente POR DELANTE del fondo (-5f) y obstáculos (-4f)
        geo.setLocalTranslation(-5f, posicionY, -3f);

        // Lo añadimos al nodo principal para que el motor lo dibuje
        rootNode.attachChild(geo);
    }

    public Geometry getGeometry() {
        return geo;
    }
}