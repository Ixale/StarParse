package com.ixale.starparse.gui.popout;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatChallenge;
import com.ixale.starparse.domain.ValueType;
import com.ixale.starparse.domain.stats.*;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.main.RaidPresenter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class DamageTakenPopoutPresenter extends BasePopoutPresenter {

	private static final String MODE_DAMAGE = "damage", MODE_HEALING = "healing";

	@FXML
	private VBox statsGrid;

	// FIXME: duplicates DamageTakenPresenter
	@FXML
	private Label
			ie, iePercent, ke, kePercent, ft, ftPercent, mr, mrPercent,
			mt, missPercent, st, shieldPercent,
			absorbedSelf, absorbedSelfPercent, absorbedOthers, absorbedOthersPercent;

	@FXML
	private GridPane modeAll;

	@FXML
	private AnchorPane statsWrapper;

	protected RaidPresenter raidPresenter;

	public void setRaidPresenter(final RaidPresenter raidPresenter) {
		this.raidPresenter = raidPresenter;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.offsetY = 790;
		this.height = 211;

		addMode(new Mode(Mode.DEFAULT, "Personal", 211, modeAll));
	}

	@Override
	protected void refreshCombatStats(final Combat combat, final CombatStats stats) throws Exception {

		if (combat == null) {
			return;
		}

		// mitigation overview
		final CombatMitigationStats mitiStats = eventService.getCombatMitigationStats(combat, context.getCombatSelection());

		if (mitiStats == null) {
			// combat gone away
			resetCombatStats();
			return;
		}

		ie.setText(Format.formatMillions(mitiStats.getInternal() + mitiStats.getElemental()));
		iePercent.setText(Format.formatFloat(mitiStats.getInternalPercent() + mitiStats.getElementalPercent()) + " %");
		ke.setText(Format.formatMillions(mitiStats.getEnergy() + mitiStats.getKinetic()));
		kePercent.setText(Format.formatFloat(mitiStats.getEnergyPercent() + mitiStats.getKineticPercent()) + " %");


		// FIXME: database
		int ftTotal = 0, mrTotal = 0, total = 0;
		List<DamageTakenStats> dtStats = eventService.getDamageTakenStats(combat,
				false, false, true,
				context.getCombatSelection());
		for (final DamageTakenStats dts: dtStats) {
			total += dts.getTotal();
			if (dts.getGuid() > 0 && context.getAttacks().containsKey(dts.getGuid())) {
				switch (context.getAttacks().get(dts.getGuid())) {
					case FT:
						ftTotal += dts.getTotal();
						break;
					case MR:
						ftTotal += dts.getTotalIe();
						mrTotal += dts.getTotal() - dts.getTotalIe();
						break;
				}
			} else {
				ftTotal += dts.getTotalIe();
			}
		}
		ft.setText(Format.formatMillions(ftTotal));
		ftPercent.setText(Format.formatFloat(total > 0 ? (ftTotal * 100.0 / total) : 0) + " %");
		mr.setText(Format.formatMillions(mrTotal));
		mrPercent.setText(Format.formatFloat(total > 0 ? (mrTotal * 100.0 / total) : 0) + " %");

		mt.setText(Format.formatNumber(mitiStats.getMissTicks()));
		st.setText(Format.formatNumber(mitiStats.getShieldTicks()));
		missPercent.setText(Format.formatFloat(mitiStats.getMissPercent()) + " %");
		shieldPercent.setText(Format.formatFloat(mitiStats.getShieldPercent()) + " %");

		absorbedSelfPercent.setText(Format.formatFloat(mitiStats.getAbsorbedSelfPercent()) + " %");
		absorbedSelf.setText(Format.formatMillions(mitiStats.getAbsorbedSelf()));
		absorbedOthersPercent.setText(Format.formatFloat(mitiStats.getAbsorbedOthersPercent()) + " %");
		absorbedOthers.setText(Format.formatMillions(mitiStats.getAbsorbedOthers()));
	}





	@Override
	public void resetCombatStats() {
		ie.setText("");
		iePercent.setText("");

		ke.setText("");
		kePercent.setText("");

		ft.setText("");
		ftPercent.setText("");
		mr.setText("");
		mrPercent.setText("");

		missPercent.setText("");
		mt.setText("");
		shieldPercent.setText("");
		st.setText("");

		absorbedSelfPercent.setText("");
		absorbedSelf.setText("");

		absorbedOthers.setText("");
		absorbedOthersPercent.setText("");
	}

	@Override
	public void setTextColor(Color color) {
		super.setTextColor(color);

		setLabelColor(color);
	}

	private void setLabelColor(Color color) {
		for (Mode m: getModes()) {
			for (final Node n: ((GridPane) m.wrapper).getChildren()) {
				if (n instanceof Text) {
					((Text) n).setFill(color);
				}
			}
		}
	}

	@Override
	public void repaint(Object source) {
		super.repaint(source);
		setLabelColor(textColor);
	}

	@Override
	protected void setHeight(int height) {
		statsWrapper.setPrefHeight(height - 20);
		super.setHeight(height);
	}
}
