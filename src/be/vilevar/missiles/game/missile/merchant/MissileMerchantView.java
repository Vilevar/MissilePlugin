package be.vilevar.missiles.game.missile.merchant;

import be.vilevar.missiles.game.missile.merchant.MissileMerchant.WeaponsMerchantStage;

public class MissileMerchantView {

	private final MissileMerchant merchant;
	private WeaponsMerchantStage stage;
	
	public MissileMerchantView(MissileMerchant merchant, WeaponsMerchantStage stage) {
		this.merchant = merchant;
		this.stage = stage;
	}
	
	public WeaponsMerchantStage getStage() {
		return stage;
	}
	
	public void setStage(WeaponsMerchantStage stage) {
		this.stage = stage;
	}
	
	public MissileMerchant getMerchant() {
		return merchant;
	}
	
	
}
