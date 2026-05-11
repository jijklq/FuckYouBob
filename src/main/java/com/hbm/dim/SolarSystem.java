package com.hbm.dim;

// STUB: full CelestialBody orbital mechanics not yet ported.
// Body enum has all 11 values so BlockOre spawn-location logic works correctly.
// Methods requiring CelestialBody (getBody, getProcessingLevel, getStoneTexture,
// getSurfaceTexture, getDimensionId) will be added with the space/dimension port.
public class SolarSystem {

	public enum Body {
		ORBIT(""),
		KERBIN("kerbin"),
		MUN("mun"),
		MINMUS("minmus"),
		DUNA("duna"),
		MOHO("moho"),
		DRES("dres"),
		EVE("eve"),
		IKE("ike"),
		LAYTHE("laythe"),
		TEKTO("tekto");
		//THATMO("thatmo"); sit this one out buddy :)

		public String name;

		Body(String name) {
			this.name = name;
		}
	}
}
