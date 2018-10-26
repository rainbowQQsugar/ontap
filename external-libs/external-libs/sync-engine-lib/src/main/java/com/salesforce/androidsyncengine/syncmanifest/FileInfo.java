package com.salesforce.androidsyncengine.syncmanifest;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

public class FileInfo {
	@NotNull
	@NotBlank
	private String binaryField;
	@NotNull
	@NotBlank
	private String nameField;
	@NotNull
	@NotBlank
	private String typeField;
	@NotBlank
	private String sizeField;

	private String[] requiredFilesFilters;

	private String[] additionalFilesFilters;
	
	public String getBinaryField() {
		return binaryField;
	}
	public void setBinaryField(String binaryField) {
		this.binaryField = binaryField;
	}
	public String getNameField() {
		return nameField;
	}
	public void setNameField(String nameField) {
		this.nameField = nameField;
	}
	public String getTypeField() {
		return typeField;
	}
	public void setTypeField(String typeField) {
		this.typeField = typeField;
	}
	public String getSizeField() {
		return sizeField;
	}
	public void setSizeField(String sizeField) {
		this.sizeField = sizeField;
	}

	public String[] getRequiredFilesFilters() {
		return requiredFilesFilters;
	}

	public void setRequiredFilesFilters(String[] requiredFilesFilters) {
		this.requiredFilesFilters = requiredFilesFilters;
	}

	public String[] getAdditionalFilesFilters() {
		return additionalFilesFilters;
	}

	public void setAdditionalFilesFilters(String[] additionalFilesFilters) {
		this.additionalFilesFilters = additionalFilesFilters;
	}
}
