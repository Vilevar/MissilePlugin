package be.vilevar.missiles.mcelements.merchant;

import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant.WeaponsMerchantStage;

public class MerchantView {

	private final WeaponsMerchant merchant;
	private WeaponsMerchantStage stage;
	
	public MerchantView(WeaponsMerchant merchant, WeaponsMerchantStage stage) {
		this.merchant = merchant;
		this.stage = stage;
	}
	
	public WeaponsMerchantStage getStage() {
		return stage;
	}
	
	public void setStage(WeaponsMerchantStage stage) {
		this.stage = stage;
	}
	
	public WeaponsMerchant getMerchant() {
		return merchant;
	}
	
	
}
