package com.ixale.starparse.domain;

import java.io.Serializable;

public class ConfigPopout implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	private Boolean enabled;
	private Double positionX, positionY;

	private Double opacity, scale, height, width;
	private Boolean bars;

	private String mode;
	private Integer cols, rows;

	public ConfigPopout(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled != null ? enabled : false;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Double getPositionX() {
		return positionX;
	}

	public void setPositionX(Double positionX) {
		this.positionX = positionX;
	}

	public Double getPositionY() {
		return positionY;
	}

	public void setPositionY(Double positionY) {
		this.positionY = positionY;
	}

	public Double getOpacity() {
		return opacity;
	}

	public void setOpacity(Double opacity) {
		this.opacity = opacity;
	}

	public Double getScale() {
		return scale;
	}

	public void setScale(Double scale) {
		this.scale = scale;
	}

	public Boolean getBars() {
		return bars;
	}

	public void setBars(Boolean bars) {
		this.bars = bars;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Integer getCols() {
		return cols;
	}

	public void setCols(Integer cols) {
		this.cols = cols;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}
}
