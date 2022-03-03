package com.ixale.starparse.gui;

import java.io.IOException;
import java.io.InputStream;

import com.ixale.starparse.gui.popout.RaidBossPopoutPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.ixale.starparse.gui.dialog.RaidNotesDialogPresenter;
import com.ixale.starparse.gui.dialog.SettingsDialogPresenter;
import com.ixale.starparse.gui.dialog.UploadParselyDialogPresenter;
import com.ixale.starparse.gui.main.CombatLogPresenter;
import com.ixale.starparse.gui.main.DamageDealtPresenter;
import com.ixale.starparse.gui.main.DamageTakenPresenter;
import com.ixale.starparse.gui.main.HealingDonePresenter;
import com.ixale.starparse.gui.main.HealingTakenPresenter;
import com.ixale.starparse.gui.main.MainPresenter;
import com.ixale.starparse.gui.main.OverviewPresenter;
import com.ixale.starparse.gui.main.RaidPresenter;
import com.ixale.starparse.gui.popout.ChallengesPopoutPresenter;
import com.ixale.starparse.gui.popout.HotsPopoutPresenter;
import com.ixale.starparse.gui.popout.PersonalStatsPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidDpsPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidHpsPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidNotesPopoutPresenter;
import com.ixale.starparse.gui.popout.RaidTpsPopoutPresenter;
import com.ixale.starparse.gui.popout.TimersCenterPopoutPresenter;
import com.ixale.starparse.gui.popout.TimersPopoutPresenter;
import com.ixale.starparse.gui.popout.TimersBPopoutPresenter;
import com.ixale.starparse.gui.popout.TimersCPopoutPresenter;

import javafx.fxml.FXMLLoader;

@Configuration
@ImportResource("classpath:spring-context.xml")
public class StarparseAppFactory
{
	@Bean
	public MainPresenter mainPresenter()
	{
		return loadPresenter("/fxml/Main.fxml");
	}

	@Bean
	public OverviewPresenter overviewPresenter()
	{
		return loadPresenter("/fxml/Overview.fxml");
	}

	@Bean
	public DamageDealtPresenter damageDealtPresenter()
	{
		return loadPresenter("/fxml/DamageDealt.fxml");
	}

	@Bean
	public HealingDonePresenter healingDonePresenter()
	{
		return loadPresenter("/fxml/HealingDone.fxml");
	}

	@Bean
	public DamageTakenPresenter damageTakenPresenter()
	{
		return loadPresenter("/fxml/DamageTaken.fxml");
	}

	@Bean
	public HealingTakenPresenter healingTakenPresenter()
	{
		return loadPresenter("/fxml/HealingTaken.fxml");
	}

	@Bean
	public CombatLogPresenter combatLogPresenter()
	{
		return loadPresenter("/fxml/CombatLog.fxml");
	}

	@Bean
	public RaidPresenter raidPresenter()
	{
		return loadPresenter("/fxml/Raid.fxml");
	}

	@Bean
	public TimersPopoutPresenter timersPopoutPresenter()
	{
		return loadPresenter("/fxml/TimersPopout.fxml", new TimersPopoutPresenter());
	}

	@Bean
	public TimersBPopoutPresenter timersBPopoutPresenter()
	{
		return loadPresenter("/fxml/TimersPopout.fxml", new TimersBPopoutPresenter());
	}

	@Bean
	public TimersCPopoutPresenter timersCPopoutPresenter()
	{
		return loadPresenter("/fxml/TimersPopout.fxml", new TimersCPopoutPresenter());
	}

	@Bean
	public TimersCenterPopoutPresenter timersCenterPopoutPresenter()
	{
		return loadPresenter("/fxml/TimersCenterPopout.fxml");
	}

	@Bean
	public PersonalStatsPopoutPresenter personalStatsPopoutPresenter()
	{
		return loadPresenter("/fxml/PersonalStatsPopout.fxml");
	}

	@Bean
	public RaidDpsPopoutPresenter raidDpsPopoutPresenter()
	{
		return loadPresenter("/fxml/RaidDpsPopout.fxml");
	}

	@Bean
	public RaidHpsPopoutPresenter raidHpsPopoutPresenter()
	{
		return loadPresenter("/fxml/RaidHpsPopout.fxml");
	}

	@Bean
	public RaidTpsPopoutPresenter raidTpsPopoutPresenter()
	{
		return loadPresenter("/fxml/RaidTpsPopout.fxml");
	}

	@Bean
	public RaidBossPopoutPresenter raidBossPopoutPresenter()
	{
		return loadPresenter("/fxml/RaidBossPopout.fxml");
	}

	@Bean
	public RaidNotesPopoutPresenter raidNotesPopoutPresenter()
	{
		return loadPresenter("/fxml/RaidNotesPopout.fxml");
	}

	@Bean
	public RaidNotesDialogPresenter RaidNotesDialogPresenter()
	{
		return loadPresenter("/fxml/RaidNotesDialog.fxml");
	}

	@Bean
	public SettingsDialogPresenter setingsDialogPresenter()
	{
		return loadPresenter("/fxml/SettingsDialog.fxml");
	}

	@Bean
	public UploadParselyDialogPresenter uploadParselyDialogPresenter()
	{
		return loadPresenter("/fxml/UploadParselyDialog.fxml");
	}

	@Bean
	public ChallengesPopoutPresenter challengesPopoutPresenter()
	{
		return loadPresenter("/fxml/ChallengesPopout.fxml");
	}

	@Bean
	public HotsPopoutPresenter hotsPopoutPresenter()
	{
		return loadPresenter("/fxml/HotsPopout.fxml");
	}

	private <T> T loadPresenter(String fxmlFile) {
		return loadPresenter(fxmlFile, null);
	}

	private <T> T loadPresenter(String fxmlFile, final T forcedPresenter)
	{
		InputStream fxmlStream = null;
		try
		{
			fxmlStream = getClass().getResourceAsStream(fxmlFile);

			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/"));
			if (forcedPresenter != null) {
				loader.setController(forcedPresenter);
				loader.load(fxmlStream);
				return forcedPresenter;
			} else {
				loader.load(fxmlStream);
				//noinspection unchecked
				return (T) loader.getController();
			}

		} catch (IOException e)
		{
			throw new RuntimeException(String.format("Unable to load FXML file '%s'", fxmlFile), e);
		} finally
		{
			if (fxmlStream != null)
			{
				try {
					fxmlStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
