package org.eclipse.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;
import java.io.File;
import java.util.ArrayList;

/**
 * The ProjectLocationSelectionDialog is the dialog used to select the name
 * and location of a project for moving or copying.
 */
public class ProjectLocationSelectionDialog extends SelectionDialog {
	// widgets
	private Text projectNameField;
	private Text locationPathField;
	private Label locationLabel;
	private IProject project;
	private Label statusMessageLabel;
	private Button browseButton;

	private static String PROJECT_NAME_LABEL = WorkbenchMessages.getString("ProjectLocationSelectionDialog.nameLabel"); //$NON-NLS-1$
	private static String LOCATION_LABEL = WorkbenchMessages.getString("ProjectLocationSelectonDialog.locationLabel"); //$NON-NLS-1$
	private static String BROWSE_LABEL = WorkbenchMessages.getString("ProjectLocationSelectionDialog.browseLabel"); //$NON-NLS-1$
	private static String DIRECTORY_DIALOG_LABEL = WorkbenchMessages.getString("ProjectLocationSelectionDialog.directoryLabel"); //$NON-NLS-1$
	private static String INVALID_LOCATION_MESSAGE = WorkbenchMessages.getString("ProjectLocationSelectionDialog.locationError"); //$NON-NLS-1$
	private static String PROJECT_LOCATION_SELECTION_TITLE = WorkbenchMessages.getString("ProjectLocationSelectionDialog.selectionTitle"); //$NON-NLS-1$

	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	private static final int SIZING_INDENTATION_WIDTH = 10;

	private boolean useDefaults = true;

/**
 * Create a ProjectLocationSelectionDialog on the supplied project parented by the parentShell.
 * @param parentShell
 * @param existingProject
 */
public ProjectLocationSelectionDialog(
	Shell parentShell,
	IProject existingProject) {
	super(parentShell);
	setTitle(PROJECT_LOCATION_SELECTION_TITLE);
	this.project = existingProject;
	try {
		this.useDefaults = this.getProject().getDescription().getLocation() == null;
	} catch (CoreException exception) {
		//Leave it as the default if we get a selection.
	}
}
/**
 * Check the message. If it is null then continue otherwise inform the user via the
 * status value and disable the OK.
 * @param message - the error message to show if it is not null.
 */
private void applyValidationResult(String errorMsg) {

	if (errorMsg == null) {
		statusMessageLabel.setText("");//$NON-NLS-1$
		getOkButton().setEnabled(true);
	} else {
		statusMessageLabel.setForeground(
			statusMessageLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
		statusMessageLabel.setText(errorMsg);
		getOkButton().setEnabled(false);
	}
}
/**
 * Check whether the entries are valid. If so return null. Otherwise
 * return a string that indicates the problem.
 */
private String checkValid() {
	String valid = checkValidName();
	if (valid != null)
		return valid;
	return checkValidLocation();
}
/**
 * Check if the entry in the widget location is valid. If it is valid return null. Otherwise
 * return a string that indicates the problem.
 */
private String checkValidLocation() {

	if (useDefaults)
		return null;
	else {
		String locationFieldContents = locationPathField.getText();
		if (!locationFieldContents.equals("")) {//$NON-NLS-1$
			IPath path = new Path("");//$NON-NLS-1$
			if (!path.isValidPath(locationFieldContents)) {
				return INVALID_LOCATION_MESSAGE;
			}
		}

		IStatus locationStatus =
			this.project.getWorkspace().validateProjectLocation(
				this.project,
				new Path(locationFieldContents));

		if (!locationStatus.isOK())
			return locationStatus.getMessage();

		return null;
	}
}
/**
 * Check if the entries in the widget are valid. If they are return null otherwise
 * return a string that indicates the problem.
 */
private String checkValidName() {

	String name = this.projectNameField.getText();
	IWorkspace workspace = getProject().getWorkspace();
	IStatus nameStatus = workspace.validateName(name, IResource.PROJECT);
	if (!nameStatus.isOK())
		return nameStatus.getMessage();
	IProject newProject = workspace.getRoot().getProject(name);
	if (newProject.exists()) {
		return WorkbenchMessages.format("CopyProjectAction.alreadyExists", new Object[] { name });
	}

	return null;
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, new Object[] {IHelpContextIds.PROJECT_LOCATION_SELECTION_DIALOG});
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createContents(Composite parent) {
	Control contents = super.createContents(parent);
	projectNameField.setFocus();
	return contents;
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	// page group
	Composite composite = (Composite) super.createDialogArea(parent);

	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));

	createProjectNameGroup(composite);
	createProjectLocationGroup(composite);

	//Add in a label for status messages if required
	statusMessageLabel = new Label(composite, SWT.NONE);
	statusMessageLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
	statusMessageLabel.setFont(parent.getFont());

	return composite;
}
/**
 * Create the listener that is used to validate the location entered by the iser
 */
private void createLocationListener() {

	Listener listener = new Listener() {
		public void handleEvent(Event event) {

			applyValidationResult(checkValid());
		}
	};

	this.locationPathField.addListener(SWT.Modify, listener);
}
/**
 * Create the listener that is used to validate the entries for the receiver
 */
private void createNameListener() {

	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			setLocationForSelection();
			applyValidationResult(checkValid());
		}
	};

	this.projectNameField.addListener(SWT.Modify, listener);
}
/**
 * Creates the project location specification controls.
 *
 * @param parent the parent composite
 */
private final void createProjectLocationGroup(Composite parent) {

	// project specification group
	Composite projectGroup = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	projectGroup.setLayout(layout);
	projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	final Button useDefaultsButton =
		new Button(projectGroup, SWT.CHECK | SWT.RIGHT);
	useDefaultsButton.setText(WorkbenchMessages.getString("ProjectLocationSelectionDialog.useDefaultLabel")); //$NON-NLS-1$
	useDefaultsButton.setSelection(this.useDefaults);
	GridData buttonData = new GridData();
	buttonData.horizontalSpan = 3;
	useDefaultsButton.setLayoutData(buttonData);

	createUserSpecifiedProjectLocationGroup(projectGroup, !this.useDefaults);

	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			useDefaults = useDefaultsButton.getSelection();
			browseButton.setEnabled(!useDefaults);
			locationPathField.setEnabled(!useDefaults);
			locationLabel.setEnabled(!useDefaults);
			setLocationForSelection();
			if (!useDefaults)
				locationPathField.setText("");
		}
	};
	useDefaultsButton.addSelectionListener(listener);
}
/**
 * Creates the project name specification controls.
 *
 * @param parent the parent composite
 */
private void createProjectNameGroup(Composite parent) {
	// project specification group
	Composite projectGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	projectGroup.setLayout(layout);
	projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	// new project label
	Label projectLabel = new Label(projectGroup,SWT.NONE);
	projectLabel.setText(PROJECT_NAME_LABEL);
	projectLabel.setFont(parent.getFont());

	// new project name entry field
	projectNameField = new Text(projectGroup, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	projectNameField.setLayoutData(data);
	
	// Set the initial value first before listener
	// to avoid handling an event during the creation.
	projectNameField.setText(getCopyNameFor(getProject().getName()));
	projectNameField.selectAll();
	
	createNameListener();
	
}
/**
 * Creates the project location specification controls.
 *
 * @return the parent of the widgets created
 * @param projectGroup the parent composite
 * @param enabled - sets the initial enabled state of the widgets
 */
private Composite createUserSpecifiedProjectLocationGroup(
	Composite projectGroup,
	boolean enabled) {

	// location label
	locationLabel = new Label(projectGroup, SWT.NONE);
	locationLabel.setText(LOCATION_LABEL);
	locationLabel.setFont(projectGroup.getFont());
	locationLabel.setEnabled(enabled);

	// project location entry field
	locationPathField = new Text(projectGroup, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	locationPathField.setLayoutData(data);
	locationPathField.setEnabled(enabled);

	// browse button
	this.browseButton = new Button(projectGroup, SWT.PUSH);
	this.browseButton.setText(BROWSE_LABEL);
	this.browseButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleLocationBrowseButtonPressed();
		}
	});
	this.browseButton.setFont(projectGroup.getFont());
	this.browseButton.setEnabled(enabled);

	// Set the initial value first before listener
	// to avoid handling an event during the creation.
	try {
		IPath location = this.getProject().getDescription().getLocation();
		if (location == null)
			setLocationForSelection();
		else
			locationPathField.setText(location.toString());
	} catch (CoreException exception) {
		//Do nothing as it is just an initialization problem
	}

	createLocationListener();
	return projectGroup;

}
/**
 * Generates a new name for the project that does not have any collisions.
 */
private String getCopyNameFor(String projectName) {

	IWorkspace workspace = getProject().getWorkspace();
	if (!workspace.getRoot().getProject(projectName).exists())
		return projectName;

	int counter = 1;
	while (true) {
		String nameSegment;
		if (counter > 1) {
			nameSegment = WorkbenchMessages.format(WorkbenchMessages.getString("CopyProjectAction.copyNameTwoArgs"), new Object[] {new Integer(counter), projectName}); //$NON-NLS-1$
		}
		else {
			nameSegment = WorkbenchMessages.format(WorkbenchMessages.getString("CopyProjectAction.copyNameOneArg"), new Object[] {projectName}); //$NON-NLS-1$
		}
	
		if (!workspace.getRoot().getProject(nameSegment).exists())
			return nameSegment;

		counter++;
	}

}
/**
 * Get the project being manipulated.
 */
private IProject getProject() {
	return this.project;
}
/**
 *	Open an appropriate directory browser
 */
private void handleLocationBrowseButtonPressed() {
	DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
	dialog.setMessage(DIRECTORY_DIALOG_LABEL);
	
	String dirName = locationPathField.getText();
	if (!dirName.equals("")) {//$NON-NLS-1$
		File path = new File(dirName);
		if (path.exists())
			dialog.setFilterPath(dirName);
	}

	String selectedDirectory = dialog.open();
	if (selectedDirectory != null)
		locationPathField.setText(selectedDirectory);
}
/**
 * The <code>ProjectLocationSelectionDialog</code> implementation of this 
 * <code>Dialog</code> method builds a two element list - the first element
 * is the project name and the second one is the location.
 */
protected void okPressed() {
	
	ArrayList list = new ArrayList();
	list.add(this.projectNameField.getText());
	if(useDefaults)
		list.add(Platform.getLocation().toString());
	else
		list.add(this.locationPathField.getText());
	setResult(list);
	super.okPressed();
}
/**
 * Set the location to the default location if we are set to useDefaults.
 */
private void setLocationForSelection() {
	if (useDefaults) {
		IPath defaultPath = Platform.getLocation().append(projectNameField.getText());
		locationPathField.setText(defaultPath.toOSString());
	}
}
}
