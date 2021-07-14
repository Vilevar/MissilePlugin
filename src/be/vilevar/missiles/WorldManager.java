package be.vilevar.missiles;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import be.vilevar.missiles.utils.NormalLaw;
import be.vilevar.missiles.utils.Vec3d;

public class WorldManager {

	private Random random = new Random();
	
	private double g = 9.8066;
	private Vec3d G = new Vec3d(0, 0, -g);
	
	private double windAngleDeg = random.nextDouble() * 360;
	private NormalLaw windAngleChange = new NormalLaw(this.random, 0, 10);
	private NormalLaw windAngleError = new NormalLaw(this.random, 0, 2.5);
	
	private WeatherCondition lastWeatherCondition;
	private long lastWeatherConditionTime;
	private double windSpeed;
	private NormalLaw windSpeedError = new NormalLaw(this.random, 0, 1);
	
	private Vec3d wind;
	
	private double clearAirDensity = 1.225;
	private double rainAirDensity = 1.221;
	private double thunderAirDensity = 1.217;
	
	private double 	gAlpha = 0.75,
					R = 100 * (10*sqrt(gAlpha) - 1) / (1 - sqrt(gAlpha)),
					GM = g * pow((R + 100), 2);
	
	public WorldManager(JavaPlugin pl, World world) {
		pl.getServer().getScheduler().scheduleSyncRepeatingTask(pl, () -> {
			this.windAngleDeg += this.windAngleChange.nextValue();
			this.windAngleDeg %= 360;
			
			double angle = Math.toRadians(this.windAngleDeg);
			this.wind = new Vec3d(Math.cos(angle), Math.sin(angle), 0);
			
			if((System.currentTimeMillis() - this.lastWeatherConditionTime) > 1400000) {
				this.updateToWeatherCondition(world);
			} else {
				this.trackWind();
			}
		}, 12000, 12000);
		this.updateToWeatherCondition(world);
	}

	public Vec3d getG() {
		return G;
	}
	
	public double getR() {
		return R;
	}
	
	public double getGM() {
		return GM;
	}
	
	public Vec3d getWind(World world) {
		if(this.getWeatherConditon(world) != this.lastWeatherCondition)
			this.updateToWeatherCondition(world);
		return wind;
	}
	
	public double checkWindAngle() {
		return (this.windAngleDeg + this.windAngleError.nextValue()) % 360;
	}
	
	public double checkWindSpeed(World world) {
		if(this.getWeatherConditon(world) != this.lastWeatherCondition)
			this.updateToWeatherCondition(world);
		return this.windSpeed + this.windSpeedError.nextValue();
	}
	
	public double getAirDensity(World world) {
		if(world.hasStorm())
			return this.rainAirDensity;
		else if(world.isThundering())
			return this.thunderAirDensity;
		else 
			return this.clearAirDensity;
	}
	
	
	private void updateToWeatherCondition(World world) {
		this.lastWeatherCondition = this.getWeatherConditon(world);
		this.lastWeatherConditionTime = System.currentTimeMillis();
		this.windSpeed = this.lastWeatherCondition.generateWind();
		this.trackWind();
	}
	
	public WeatherCondition getWeatherConditon(World world) {
		if(world.getTime() < 13000 || world.getTime() == 24000) {
			if(world.isThundering()) {
				return WeatherCondition.DAY_THUNDER;
			} else if(world.hasStorm()) {
				return WeatherCondition.DAY_RAIN;
			} else {
				return WeatherCondition.DAY_CLEAR;
			}
		} else {
			if(world.isThundering()) {
				return WeatherCondition.NIGHT_THUNDER;
			} else if(world.hasStorm()) {
				return WeatherCondition.NIGHT_RAIN;
			} else {
				return WeatherCondition.NIGHT_CLEAR;
			}
		}
	}
	
	private void trackWind() {
		double angle = Math.toRadians(this.windAngleDeg);
		this.wind = new Vec3d(Math.cos(angle), Math.sin(angle), 0).multiply(this.windSpeed);
	}
}
