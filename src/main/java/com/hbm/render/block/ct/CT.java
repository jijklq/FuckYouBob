package com.hbm.render.block.ct;

//~ stealth-todo: full CT (connected-texture) render system deferred to render phase
//~ context: only renderID constant referenced; Bob's quadrant constants (l,r,t,b,f,c,j,h,v) and math retained in 1.7.10 source — add back during render phase
// Bob's: renderID = RenderingRegistry.getNextAvailableRenderId() — no RenderingRegistry in 1.12.2; IBakedModel used instead
public class CT {
    public static int renderID = -1;
}
