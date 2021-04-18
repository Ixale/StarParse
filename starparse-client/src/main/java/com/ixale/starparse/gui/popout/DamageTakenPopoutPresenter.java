package com.ixale.starparse.gui.popout;

import com.ixale.starparse.domain.Combat;
import com.ixale.starparse.domain.CombatSelection;
import com.ixale.starparse.domain.stats.*;
import com.ixale.starparse.gui.Format;
import com.ixale.starparse.gui.main.RaidPresenter;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Arrays;
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
	private Label ieInstant1, ieInstant5, keInstant1, keInstant5, ftInstant1, ftInstant5, mrInstant1, mrInstant5;

	@FXML
	private Label delay1, delay2;

	@FXML
	private GridPane modeAll, modeInstant;

	@FXML
	private AnchorPane statsWrapper;

	private Integer dtDelay1 = 2, dtDelay2 = 10;

	protected RaidPresenter raidPresenter;

	public void setRaidPresenter(final RaidPresenter raidPresenter) {
		this.raidPresenter = raidPresenter;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		super.initialize(url, resourceBundle);

		this.offsetY = 790;
		this.height = 211;

		addMode(new Mode(Mode.DEFAULT, "Total Taken", 211, modeAll));
		addMode(new Mode(Mode.DEFAULT, "Currently Taken", 122, modeInstant));
	}

	public static CombatSelection computeSelectionForPastMillis(final Combat combat, final CombatStats stats, int millisDelay) throws Exception {
		final long currentTick = stats.getTick();
		final CombatSelection combatSel = new CombatSelection(
				combat.getEventIdFrom(),
				combat.getEventIdTo(),
				currentTick > millisDelay? currentTick - millisDelay : 0,
				currentTick
				);
		return combatSel;
	}

	// ie, ke, ft, mr
	private String [] getDamageTexts(final Combat combat, final CombatStats stats, int millisDelay) throws Exception {
		String [] result = new String [4];
		CombatSelection combatSelection = computeSelectionForPastMillis(combat, stats, millisDelay);
		final CombatMitigationStats mitiStats = eventService.getCombatMitigationStats(combat, combatSelection);
		final List<DamageTakenStats> dtStats = eventService.getDamageTakenStats(combat,
				false, false, true, combatSelection);
		List<Integer> totals = this.getTotals(dtStats);
		int ftTotal = totals.get(0), mrTotal = totals.get(1), total = totals.get(2);

		result [0] = Format.formatMillions(mitiStats.getInternal() + mitiStats.getElemental()); // ie
		result [1] = Format.formatMillions(mitiStats.getEnergy() + mitiStats.getKinetic());     // ke
		result [2] = Format.formatMillions(ftTotal);	// ft
		result [3] = Format.formatMillions(mrTotal);	// mr
		return result;
	}

	private List<Integer> getTotals(List<DamageTakenStats> dtStats) {
		int ftTotal = 0, mrTotal = 0, total = 0;
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
		return Arrays.asList(ftTotal, mrTotal, total);
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


		// FIXME: use object
		List<DamageTakenStats> dtStats = eventService.getDamageTakenStats(combat,
				false, false, true,
				context.getCombatSelection());
		List<Integer> totals = this.getTotals(dtStats);
		int ftTotal = totals.get(0), mrTotal = totals.get(1), total = totals.get(2);

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

		String[] damageTexts1 = getDamageTexts(combat, stats, dtDelay1 == null ? 2000 : dtDelay1 * 1000);
		ieInstant1.setText(damageTexts1[0]);
		keInstant1.setText(damageTexts1[1]);
		ftInstant1.setText(damageTexts1[2]);
		mrInstant1.setText(damageTexts1[3]);
		String[] damageTexts5 = getDamageTexts(combat, stats, dtDelay2 == null ? 10000 : dtDelay2 * 1000);
		ieInstant5.setText(damageTexts5[0]);
		keInstant5.setText(damageTexts5[1]);
		ftInstant5.setText(damageTexts5[2]);
		mrInstant5.setText(damageTexts5[3]);
	}





	@Override
	public void resetCombatStats() {
		delay1.setText("last "+dtDelay1+"s");
		delay2.setText("last "+dtDelay2+"s");

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

		//, , , , , , ,
		ieInstant1.setText("");
		keInstant1.setText("");
		ftInstant1.setText("");
		mrInstant1.setText("");

		ieInstant5.setText("");
		keInstant5.setText("");
		ftInstant5.setText("");
		mrInstant5.setText("");

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

    public void setDtDelay1(Integer dtDelay1) {
		if (dtDelay1 != null) {
			this.dtDelay1 = dtDelay1;
			this.delay1.setText("last "+dtDelay1+"s");
		}
    }

    public void setDtDelay2(Integer dtDelay2) {
		if (dtDelay2 != null) {
			this.dtDelay2 = dtDelay2;
			this.delay2.setText("last "+dtDelay2+"s");
		}
    }
}
