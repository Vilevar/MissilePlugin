package be.vilevar.missiles;

import be.vilevar.missiles.utils.NormalLaw;

public enum WeatherCondition {

	DAY_CLEAR(new NormalLaw(3.9, 1.5)),
	DAY_RAIN(new NormalLaw(11, 2.3)),
	DAY_THUNDER(new NormalLaw(25, 2.9)),
	NIGHT_CLEAR(new NormalLaw(3, 1)),
	NIGHT_RAIN(new NormalLaw(17, 2)),
	NIGHT_THUNDER(new NormalLaw(28, 1.5));
	
	private NormalLaw wind;
	private WeatherCondition(NormalLaw wind) {
		this.wind = wind;
	}
	
	public double generateWind() {
		return Main.clamp(0, 32.5, this.wind.nextValue());
	}
	
}
