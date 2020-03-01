package com.arkivanov.mvikotlin.timetravel

import com.arkivanov.mvikotlin.core.internal.rx.observer
import com.arkivanov.mvikotlin.core.rx.Disposable
import com.arkivanov.mvikotlin.core.store.StoreEventType
import com.arkivanov.mvikotlin.timetravel.controller.timeTravelController
import com.arkivanov.mvikotlin.utils.internal.DeepStringMode
import com.arkivanov.mvikotlin.utils.internal.toDeepString
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.convert
import platform.Foundation.NSIndexPath
import platform.UIKit.*
import platform.darwin.NSInteger
import platform.darwin.NSObject
import platform.darwin.sel_registerName

@Suppress("FunctionName")
fun TimeTravelViewController(): TimeTravelViewControllerImpl = TimeTravelViewControllerImpl()

@ExportObjCClass(name = "TimeTravelControllerImpl")
class TimeTravelViewControllerImpl : UIViewController(nibName = null, bundle = null) {

    private lateinit var tableView: UITableView
    private lateinit var buttonsStack: UIStackView
    private lateinit var startRecordingButton: UIButton
    private lateinit var stopRecordingButton: UIButton
    private lateinit var moveToStartButton: UIButton
    private lateinit var stepBackwardButton: UIButton
    private lateinit var stepForwardButton: UIButton
    private lateinit var moveToEndButton: UIButton
    private lateinit var cancelButton: UIButton
    private var state: TimeTravelState = timeTravelController.state
    private var disposable: Disposable? = null

    override fun viewDidLoad() {
        super.viewDidLoad()

        setupTableView()
        setupButtons()
        alignViews()
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)

        disposable = timeTravelController.states(observer(onNext = ::onNewState))
    }

    private fun onNewState(state: TimeTravelState) {
        this.state = state
        updateButtons()
        tableView.reloadData()
    }

    override fun viewDidDisappear(animated: Boolean) {
        disposable?.dispose()
        disposable = null

        super.viewDidDisappear(animated)
    }

    private fun updateButtons() {
        when (state.mode) {
            TimeTravelState.Mode.IDLE -> {
                startRecordingButton.setHidden(false)
                stopRecordingButton.setHidden(true)
                moveToStartButton.setHidden(true)
                stepBackwardButton.setHidden(true)
                stepForwardButton.setHidden(true)
                moveToEndButton.setHidden(true)
                cancelButton.setHidden(true)
            }
            TimeTravelState.Mode.RECORDING -> {
                startRecordingButton.setHidden(true)
                stopRecordingButton.setHidden(false)
                moveToStartButton.setHidden(true)
                stepBackwardButton.setHidden(true)
                stepForwardButton.setHidden(true)
                moveToEndButton.setHidden(true)
                cancelButton.setHidden(false)
            }
            TimeTravelState.Mode.STOPPED -> {
                startRecordingButton.setHidden(true)
                stopRecordingButton.setHidden(true)
                moveToStartButton.setHidden(false)
                stepBackwardButton.setHidden(false)
                stepForwardButton.setHidden(false)
                moveToEndButton.setHidden(false)
                cancelButton.setHidden(false)
            }
        }.let {}
    }

    private fun setupTableView() {
        tableView = UITableView()
        tableView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(tableView)
        tableView.delegate = TableViewDelegate()
        tableView.dataSource = TableViewDataSource()
        tableView.rowHeight = UITableViewAutomaticDimension
    }

    private fun setupButtons() {
        startRecordingButton = createButton(imageSystemName = "circle.fill", clickAction = "onStartRecordingClick")
        stopRecordingButton = createButton(imageSystemName = "stop.fill", clickAction = "onStopRecordingClick")
        moveToStartButton = createButton(imageSystemName = "backward.end.fill", clickAction = "onMoveToStartClick")
        stepBackwardButton = createButton(imageSystemName = "chevron.left", clickAction = "onStepBackwardClick")
        stepForwardButton = createButton(imageSystemName = "chevron.right", clickAction = "onStepForwardClick")
        moveToEndButton = createButton(imageSystemName = "forward.end.fill", clickAction = "onMoveToEndClick")
        cancelButton = createButton(imageSystemName = "xmark", clickAction = "onCancelClick")

        buttonsStack = UIStackView()
        buttonsStack.axis = UILayoutConstraintAxisHorizontal
        buttonsStack.distribution = UIStackViewDistributionFillEqually
        buttonsStack.alignment = UIStackViewAlignmentCenter
        buttonsStack.spacing = 16.0
        buttonsStack.addArrangedSubview(startRecordingButton)
        buttonsStack.addArrangedSubview(stopRecordingButton)
        buttonsStack.addArrangedSubview(moveToStartButton)
        buttonsStack.addArrangedSubview(stepBackwardButton)
        buttonsStack.addArrangedSubview(stepForwardButton)
        buttonsStack.addArrangedSubview(moveToEndButton)
        buttonsStack.addArrangedSubview(cancelButton)
        buttonsStack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(buttonsStack)
    }

    @ObjCAction
    private fun onStartRecordingClick() {
        timeTravelController.startRecording()
    }

    @ObjCAction
    private fun onStopRecordingClick() {
        timeTravelController.stopRecording()
    }

    @ObjCAction
    private fun onMoveToStartClick() {
        timeTravelController.moveToStart()
    }

    @ObjCAction
    private fun onStepBackward() {
        timeTravelController.stepBackward()
    }

    @ObjCAction
    private fun onStepForward() {
        timeTravelController.stepForward()
    }

    @ObjCAction
    private fun onMoveToEndClick() {
        timeTravelController.moveToEnd()
    }

    @ObjCAction
    private fun onCancelClick() {
        timeTravelController.cancel()
    }

    private fun createButton(imageSystemName: String, clickAction: String): UIButton {
        val button = UIButton.buttonWithType(buttonType = UIButtonTypeSystem)
        button.setTitle(title = title, forState = UIControlStateNormal)
        button.addTarget(target = this, action = sel_registerName(clickAction), forControlEvents = UIControlEventTouchUpInside)
        button.setImage(UIImage.systemImageNamed(name = imageSystemName), forState = UIControlStateNormal)

        return button
    }

    private fun alignViews() {
        tableView.topAnchor.constraintEqualToAnchor(anchor = view.topAnchor).setActive(true)
        tableView.leftAnchor.constraintEqualToAnchor(anchor = view.leftAnchor).setActive(true)
        tableView.rightAnchor.constraintEqualToAnchor(anchor = view.rightAnchor).setActive(true)
        tableView.bottomAnchor.constraintEqualToAnchor(anchor = buttonsStack.topAnchor).setActive(true)

        buttonsStack.leftAnchor.constraintEqualToAnchor(anchor = view.leftAnchor).setActive(true)
        buttonsStack.rightAnchor.constraintEqualToAnchor(anchor = view.rightAnchor).setActive(true)
        buttonsStack.bottomAnchor.constraintEqualToAnchor(anchor = view.bottomAnchor).setActive(true)
    }

    private inner class TableViewDelegate : NSObject(), UITableViewDelegateProtocol {
        override fun tableView(tableView: UITableView, didSelectRowAtIndexPath: NSIndexPath) {
            val state = this@TimeTravelViewControllerImpl.state
            val item = state.events[didSelectRowAtIndexPath.row.toInt()]
            val value = item.value

            val alert =
                UIAlertController.alertControllerWithTitle(
                    title = item.storeName,
                    message = value.toDeepString(mode = DeepStringMode.FULL, format = true),
                    preferredStyle = UIAlertControllerStyleAlert
                )

            alert.addAction(UIAlertAction.actionWithTitle(title = "Close", style = UIAlertActionStyleCancel, handler = null))

            this@TimeTravelViewControllerImpl.presentViewController(viewControllerToPresent = alert, animated = true, completion = null)
        }
    }

    private inner class TableViewDataSource : NSObject(), UITableViewDataSourceProtocol {
        @Suppress("CONFLICTING_OVERLOADS")
        override fun tableView(tableView: UITableView, cellForRowAtIndexPath: NSIndexPath): UITableViewCell {
            val cell = tableView.dequeueReusableCellWithIdentifier(identifier = "TimeTravelEvent") as? Cell ?: Cell()

            val state = state
            val item = state.events[cellForRowAtIndexPath.row.toInt()]
            val value = item.value
            cell.event = item
            cell.myTitle.text = "${item.storeName} (${item.type.name})"
            cell.myText.text = value.toDeepString(mode = DeepStringMode.SHORT)
            cell.myDebugButton.setHidden(item.type == StoreEventType.STATE)

            cell.backgroundColor =
                if (state.selectedEventIndex == cellForRowAtIndexPath.row.toInt()) colorSelectedCell else UIColor.whiteColor

            return cell
        }

        @Suppress("CONFLICTING_OVERLOADS")
        override fun tableView(tableView: UITableView, numberOfRowsInSection: NSInteger): NSInteger = state.events.size.convert()
    }

    @ExportObjCClass
    private class Cell : UITableViewCell(style = UITableViewCellStyle.UITableViewCellStyleSubtitle, reuseIdentifier = "TimeTravelEvent") {
        val myTitle = UILabel()
        val myText = UILabel()
        val myDebugButton = UIButton()
        var event: TimeTravelEvent? = null

        init {
            myTitle.translatesAutoresizingMaskIntoConstraints = false
            myText.translatesAutoresizingMaskIntoConstraints = false
            myDebugButton.translatesAutoresizingMaskIntoConstraints = false

            myTitle.font = UIFont.systemFontOfSize(16.0)
            myText.font = UIFont.systemFontOfSize(12.0)
            myText.numberOfLines = 3
            myDebugButton.setImage(UIImage.systemImageNamed("play.fill"), forState = UIControlStateNormal)
//            myDebugButton.addTarget(
//                target = this,
//                action = sel_registerName("onDebugClick"),
//                forControlEvents = UIControlEventTouchUpInside
//            )

            contentView.addSubview(myTitle)
            contentView.addSubview(myText)
            contentView.addSubview(myDebugButton)

            myTitle.topAnchor.constraintEqualToAnchor(anchor = contentView.topAnchor, constant = 8.0).setActive(true)
            myTitle.leadingAnchor.constraintEqualToAnchor(anchor = contentView.leadingAnchor, constant = 8.0).setActive(true)
            myTitle.trailingAnchor.constraintEqualToAnchor(anchor = myDebugButton.leadingAnchor).setActive(true)

            myText.topAnchor.constraintEqualToAnchor(anchor = myTitle.bottomAnchor, constant = 4.0).setActive(true)
            myText.leadingAnchor.constraintEqualToAnchor(anchor = contentView.leadingAnchor, constant = 8.0).setActive(true)
            myText.trailingAnchor.constraintEqualToAnchor(anchor = contentView.trailingAnchor, constant = -8.0).setActive(true)
            myText.bottomAnchor.constraintEqualToAnchor(anchor = contentView.bottomAnchor, constant = -8.0).setActive(true)

            myDebugButton.trailingAnchor.constraintEqualToAnchor(anchor = contentView.trailingAnchor).setActive(true)
            myDebugButton.topAnchor.constraintEqualToAnchor(anchor = contentView.topAnchor).setActive(true)
            myDebugButton.widthAnchor.constraintEqualToConstant(c = 32.0).setActive(true)
            myDebugButton.heightAnchor.constraintEqualToConstant(c = 32.0).setActive(true)
        }

        @ObjCAction
        private fun onDebugClick() {
            val event = event ?: return
            timeTravelController.debugEvent(event)
        }
    }

    private companion object {
        private val colorSelectedCell = UIColor(red = 0.9, green = 0.9, blue = 0.9, alpha = 1.0)
    }
}
