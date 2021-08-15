package be.vilevar.missiles.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class Vec3d implements Cloneable {
	
	private double x;
	private double y;
	private double z;
	
	public Vec3d(Location loc) {
		this.x = loc.getX();
		this.y = loc.getZ();
		this.z = loc.getY();
	}
	
	public Vec3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public Vec3d setX(double x) {
		this.x = x;
		return this;
	}
	
	public Vec3d setY(double y) {
		this.y = y;
		return this;
	}
	
	public Vec3d setZ(double z) {
		this.z = z;
		return this;
	}
	
/*	public double alpha() {
		double x = this.getX();
		double y = this.getY();
		if(x == 0)
			return y > 0 ? Math.PI : -Math.PI;
		double alpha = Math.atan(this.y.divide(this.x, CTX).doubleValue());
		if(alpha < 0 && y > 0)
			alpha += Math.PI;
		else if(alpha > 0 && x < 0)
			alpha -= Math.PI;
		return alpha;
	}*/
	
	public double length() {
		return Math.sqrt(this.squaredLength());
	}
	
	public double squaredLength() {
		return x*x + y*y + z*z;
	}
	
	public Vec3d add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vec3d multiply(double a) {
		this.x *= a;
		this.y *= a;
		this.z *= a;
		return this;
	}
	
	public Vec3d divide(double a) {
		this.x /= a;
		this.y /= a;
		this.z /= a;
		return this;
	}
	
	public Vec3d normalize() {
		return this.divide(this.length());
	}
	
	public Vec3d negate() {
		return this.multiply(-1);
	}
	
	public double dot(Vec3d other) {
		return other.x*x + other.y*y + other.z*z; 
	}
	
	public double distance(Vec3d other) {
		return this.clone().subtract(other).length();
	}
	
	public double cosAngleWith(Vec3d other) {
		return this.dot(other) / (this.length() * other.length());
	}
	
	public double angleWith(Vec3d other) {
		return Math.acos(this.cosAngleWith(other));
	}
	
	public Vec3d add(Vec3d other) {
		return this.add(other.x, other.y, other.z);
	}
	
	public Vec3d subtract(Vec3d other) {
		return this.add(-other.x, -other.y, -other.z);
	}
	
	public Vec3d matrixProduct(double[][] matrix) {
		double[] comp = {this.x, this.y, this.z};
		this.x = 0;
		this.y = 0;
		this.z = 0;
		for(int i = 0; i < 3; i++) {
			this.x += matrix[0][i] * comp[i];
		}
		for(int i = 0; i < 3; i++) {
			this.y += matrix[1][i] * comp[i];
		}
		for(int i = 0; i < 3; i++) {
			this.z += matrix[2][i] * comp[i];
		}
		return this;
	}

	public Location toLocation(World world) {
		return new Location(world, this.x, this.z, this.y);
	}
	
	@Override
	public Vec3d clone() {
		return new Vec3d(x, y, z);
	}
	
	public boolean equals(Vec3d other) {
		return this.x == other.x && this.y == other.y && this.z == other.z;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vec3d && this.equals((Vec3d) obj);
	}
	
	@Override
	public String toString() {
		return "Vec3d["+x+"; "+y+"; "+z+"]";
	}
	
	public String basicString() {
		return "("+x+"; "+y+"; "+z+")";
	}
}
