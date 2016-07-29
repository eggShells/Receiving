package com.blockhouse.android.poreceiving;

public class PrtRecord {

	private int labelQty = 0;
	private double itemQty = 0;
	
	public PrtRecord(final int inLabelQty, final double inItemQty) {
		labelQty = inLabelQty;
		itemQty = inItemQty;
	}

	/**
	 * Gets the Print label Qty for this record.
	 * @return Number of labels to print.
	 */
	public int getLabelQty() {
		return labelQty;
	}

	/**
	 * Gets the item qty on the label to print
	 * @return The qty on the label to print.
	 */
	public double getItemQty() {
		return itemQty;
	}

	/**
	 * @param labelQty the labelQty to set
	 */
	public void setLabelQty(int labelQty) {
		this.labelQty = labelQty;
	}

	/**
	 * @param itemQty the itemQty to set
	 */
	public void setItemQty(double itemQty) {
		this.itemQty = itemQty;
	}
	
	@Override
	public String toString() {
		return "PrtRecord - Label Qty: " + labelQty + ", Item Qty: " + itemQty;
	}
	
}
