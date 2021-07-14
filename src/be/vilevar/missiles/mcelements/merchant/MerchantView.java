package be.vilevar.missiles.mcelements.merchant;

import be.vilevar.missiles.mcelements.merchant.WeaponsMerchant.WeaponsMerchantStage;

public class MerchantView {

	private final WeaponsMerchant merchant;
	private WeaponsMerchant.WeaponsMerchantStage stage;
	
	public MerchantView(WeaponsMerchant merchant, WeaponsMerchantStage stage) {
		this.merchant = merchant;
		this.stage = stage;
	}
	
	public WeaponsMerchant.WeaponsMerchantStage getStage() {
		return stage;
	}
	
	public void setStage(WeaponsMerchant.WeaponsMerchantStage stage) {
		this.stage = stage;
	}
	
	public WeaponsMerchant getMerchant() {
		return merchant;
	}
	
	
}
