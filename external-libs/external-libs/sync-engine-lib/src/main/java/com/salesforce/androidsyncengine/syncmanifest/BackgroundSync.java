/**
 * 
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce.com, inc. 
 *
 */
package com.salesforce.androidsyncengine.syncmanifest;

public class BackgroundSync {

	private boolean wifi = true;
	private boolean _3G = false;
	private boolean _4G = true;
	private boolean edge = false;
	private boolean lte = true;
	
	public boolean isWifi() {
		return wifi;
	}
	public void setWifi(boolean wifi) {
		this.wifi = wifi;
	}
	public boolean is_3G() {
		return _3G;
	}
	public void set3G(boolean _3g) {
		_3G = _3g;
	}
	public boolean is_4G() {
		return _4G;
	}
	public void set4G(boolean _4g) {
		_4G = _4g;
	}
	public boolean isEdge() {
		return edge;
	}
	public void setEdge(boolean edge) {
		this.edge = edge;
	}
	public boolean isLte() {
		return lte;
	}
	public void setLte(boolean lte) {
		this.lte = lte;
	}
}
