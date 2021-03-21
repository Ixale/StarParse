package com.ixale.starparse.gui;

import java.io.IOException;
import java.io.InputStream;

import com.ixale.starparse.gui.popout.*;
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
		return loadPresenter("/fxml/TimersPopout.fxml");
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
	public DamageTakenPopoutPresenter damageTakenPopoutPresenter()
	{
		return loadPresenter("/fxml/DamageTakenPopout.fxml");
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

	@SuppressWarnings("unchecked")
	private <T> T loadPresenter(String fxmlFile)
	{
		InputStream fxmlStream = null;
		try
		{
			fxmlStream = getClass().getResourceAsStream(fxmlFile);

			FXMLLoader loader = new FXMLLoader(Class.class.getResource("/fxml/"));
			loader.load(fxmlStream);
			return (T) loader.getController();
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
